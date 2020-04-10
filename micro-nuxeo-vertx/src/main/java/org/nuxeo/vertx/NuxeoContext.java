package org.nuxeo.vertx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.NuxeoPrincipalImpl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.impl.OAuth2TokenImpl;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

public class NuxeoContext extends RoutingContextDecorator {

    private CoreSessionClient nuxeoClient;

    private Map<String, Object> contextCache = new HashMap<>();

    public NuxeoContext(RoutingContext rc, CoreSessionClient coreSessionClient) {
        super(rc.currentRoute(), rc);
        this.nuxeoClient = coreSessionClient;

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

    public void session(Handler<AsyncResult<CloseableCoreSession>> resultHandler) {
        getPrincipal(principal -> {
            nuxeoClient.session(principal.result(), h -> {
                CloseableCoreSession session = h.result();
                resultHandler.handle(Future.succeededFuture(session));
                session.save();
            });
            nuxeoClient.close();
        });
    }

    public void query(String query, JsonObject options, Handler<AsyncResult<List<DocumentModel>>> resultHandler) {
        getPrincipal(principal -> {
            nuxeoClient.session(principal.result(), h -> {
                CloseableCoreSession session = h.result();
                resultHandler.handle(Future.succeededFuture(session.query(query)));
                session.save();
            });
            nuxeoClient.close();
        });
    }

    public Map<String, Object> getCache() {
        return contextCache;
    }

}
