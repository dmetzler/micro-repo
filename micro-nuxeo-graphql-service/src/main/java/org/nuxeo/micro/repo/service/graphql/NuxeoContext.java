package org.nuxeo.micro.repo.service.graphql;

import java.util.HashMap;
import java.util.Map;

import org.jboss.el.ExpressionFactoryImpl;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.NuxeoPrincipalImpl;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.proto.utils.GrpcInterceptor;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.impl.OAuth2TokenImpl;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

public class NuxeoContext extends RoutingContextDecorator implements NuxeoGraphqlContext {

    private final NuxeoCoreSessionVertxStub nuxeoSession;

    private Map<String, Object> contextCache = new HashMap<>();

    private ExpressionEvaluator el;

    protected NuxeoPrincipal principal;

    private SchemaManager schemaManager;

    public NuxeoContext(RoutingContext rc, NuxeoCoreSessionVertxStub nuxeoSession, SchemaManager schemaManager) {
        super(rc.currentRoute(), rc);
        this.nuxeoSession = nuxeoSession;
        this.schemaManager = schemaManager;
        el = new ExpressionEvaluator(new ExpressionFactoryImpl());

    }

    // Should probably be in a dedicated SessionHandler
    public void getPrincipal(Handler<AsyncResult<NuxeoPrincipal>> completionHandler) {
        if (principal == null) {
            User user = user();
            if (user != null) {
                if (user instanceof OAuth2TokenImpl) {
                    ((OAuth2TokenImpl) user).userInfo(userInfo -> {
                        if (userInfo.succeeded()) {
                            principal = buildPrincipal(userInfo.result().getString("email"));
                            completionHandler.handle(Future.succeededFuture(principal));
                        } else {
                            completionHandler.handle(Future.failedFuture(userInfo.cause()));
                        }
                    });
                } else {
                    principal = buildPrincipal(user.principal().getString("email"));
                    completionHandler.handle(Future.succeededFuture(principal));
                }
            } else {
                principal = new NuxeoPrincipalImpl("anonymous", "tenants");
                completionHandler.handle(Future.succeededFuture(principal));
            }
        } else {
            completionHandler.handle(Future.succeededFuture(principal));
        }
    }

    private NuxeoPrincipalImpl buildPrincipal(String email) {
        NuxeoPrincipalImpl p = new NuxeoPrincipalImpl(email, "tenants");
        p.setAdministrator(email.endsWith("@nuxeo.com"));
        return p;
    }

    public void session(Handler<AsyncResult<NuxeoCoreSessionVertxStub>> resultHandler) {
        getPrincipal(principalResult -> {
            if (principalResult.succeeded()) {

                Metadata headers = new Metadata();
                headers.put(GrpcInterceptor.PRINCIPALID_METADATA_KEY, principalResult.result().getPrincipalId());

                NuxeoCoreSessionVertxStub session = nuxeoSession.withInterceptors(
                        MetadataUtils.newAttachHeadersInterceptor(headers));

                resultHandler.handle(Future.succeededFuture(session));
            } else {
                resultHandler.handle(Future.failedFuture(principalResult.cause()));
            }

        });
    }

    public Map<String, Object> getCache() {
        return contextCache;
    }

    @Override
    public ExpressionEvaluator getEvaluator() {
        return el;
    }

    @Override
    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

}
