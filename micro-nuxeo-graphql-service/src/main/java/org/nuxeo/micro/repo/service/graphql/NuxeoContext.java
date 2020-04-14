package org.nuxeo.micro.repo.service.graphql;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.NuxeoPrincipalImpl;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.impl.OAuth2TokenImpl;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;
import io.vertx.grpc.VertxChannelBuilder;

public class NuxeoContext extends RoutingContextDecorator implements NuxeoGraphqlContext {

    private final NuxeoCoreSessionVertxStub nuxeoSession;

    public static final Metadata.Key<String> PRINCIPAL_ID_KEY = Metadata.Key.of("principalId", ASCII_STRING_MARSHALLER);

    private Map<String, Object> contextCache = new HashMap<>();

    public NuxeoContext(RoutingContext rc, NuxeoCoreSessionVertxStub nuxeoSession) {
        super(rc.currentRoute(), rc);
        this.nuxeoSession = nuxeoSession;

    }

    // Should probably by in a dedicated SessionHandler
    public void getPrincipal(Handler<AsyncResult<NuxeoPrincipal>> completionHandler) {

        User user = user();
        if (user != null) {
            if (user instanceof OAuth2TokenImpl) {
                ((OAuth2TokenImpl) user).userInfo(userInfo -> {
                    if (userInfo.succeeded()) {
                        NuxeoPrincipalImpl p = getPrincipal(userInfo.result().getString("email"));
                        completionHandler.handle(Future.succeededFuture(p));
                    } else {
                        completionHandler.handle(Future.failedFuture(userInfo.cause()));
                    }
                });
            } else {
                NuxeoPrincipalImpl p = getPrincipal(user.principal().getString("email"));
                completionHandler.handle(Future.succeededFuture(p));
            }
        } else {
            NuxeoPrincipalImpl p = new NuxeoPrincipalImpl("anonymous", "tenants");
            completionHandler.handle(Future.succeededFuture(p));
        }
    }

    private NuxeoPrincipalImpl getPrincipal(String email) {
        NuxeoPrincipalImpl p = new NuxeoPrincipalImpl(email, "tenants");
        p.setAdministrator(email.endsWith("@nuxeo.com"));
        return p;
    }

    public void session(Handler<AsyncResult<NuxeoCoreSessionVertxStub>> resultHandler) {
        getPrincipal(principalResult -> {

            Metadata headers = new Metadata();
            NuxeoPrincipal principal = principalResult.result();
            headers.put(PRINCIPAL_ID_KEY, principal.getPrincipalId());

            NuxeoCoreSessionVertxStub session = nuxeoSession.withInterceptors(
                    MetadataUtils.newAttachHeadersInterceptor(headers));

            resultHandler.handle(Future.succeededFuture(session));

        });
    }

    public Map<String, Object> getCache() {
        return contextCache;
    }

    @Override
    public NuxeoCoreSessionVertxStub getSession() {
        return nuxeoSession;
    }

    @Override
    public ExpressionEvaluator getEvaluator() {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaManager getSchemaManager() {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getPrincipal() {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

}
