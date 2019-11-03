package org.nuxeo.vertx;

import java.util.List;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.micro.NuxeoPrincipalImpl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

public class NuxeoContext extends RoutingContextDecorator {

    private CoreSessionClient nuxeoClient;

    public NuxeoContext(RoutingContext rc, CoreSessionClient coreSessionClient) {
        super(rc.currentRoute(), rc);
        this.nuxeoClient = coreSessionClient;

    }

    // Should probably by in a dedicated SessionHandler
    public void getPrincipal(Handler<AsyncResult<NuxeoPrincipal>> completionHandler) {

        NuxeoPrincipalImpl user = new NuxeoPrincipalImpl("dmetzler", "bla");
        user.setAdministrator(true);
        completionHandler.handle(Future.succeededFuture(user));
//        NuxeoPrincipalImpl principal = session().get("principal");
//
//        if (principal == null) {
//
//            AccessToken user = (AccessToken) user();
//
//            user.userInfo(res -> {
//                if (res.failed()) {
//                    // request didn't succeed because the token was revoked so we
//                    // invalidate the token stored in the session and render the
//                    // index page so that the user can start the OAuth flow again
//                    session().destroy();
//                    completionHandler.handle(Future.failedFuture(res.cause()));
//                } else {
//                    // the request succeeded, so we use the API to fetch the user's emails
//                    // fetch the user emails from the github API
//                    // the fetch method will retrieve any resource and ensure the right
//                    // secure headers are passed.
//                    user.fetch("https://api.github.com/user/emails", res2 -> {
//                        if (res2.failed()) {
//                            // request didn't succeed because the token was revoked so we
//                            // invalidate the token stored in the session and render the
//                            // index page so that the user can start the OAuth flow again
//                            session().destroy();
//                            completionHandler.handle(Future.failedFuture(res.cause()));
//                        } else {
//
//                            String email = res2.result()//
//                                    .jsonArray()//
//                                    .stream()//
//                                    .map(o -> ((JsonObject) o))//
//                                    .filter(o -> o.getBoolean("primary"))//
//                                    .map(o -> o.getString("email"))//
//                                    .findAny()//
//                                    .get();
//
//                            NuxeoPrincipalImpl p = new NuxeoPrincipalImpl(email, "tenants");
//                            p.setAdministrator(true);
//                            session().put("principal", p);
//
//                            completionHandler.handle(Future.succeededFuture(p));
//                        }
//                    });
//                }
//            });
//
//        } else {
//            completionHandler.handle(Future.succeededFuture(principal));
//        }
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

}
