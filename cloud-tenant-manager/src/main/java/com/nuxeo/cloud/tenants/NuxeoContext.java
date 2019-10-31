package com.nuxeo.cloud.tenants;

import java.util.List;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.micro.NuxeoPrincipalImpl;

import com.nuxeo.cloud.tenants.nuxeo.CoreSessionClient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

public class NuxeoContext extends RoutingContextDecorator {

    private NuxeoPrincipalImpl principal;
    private CoreSessionClient nuxeoClient;

    public NuxeoContext(RoutingContext rc, CoreSessionClient coreSessionClient) {
        super(rc.currentRoute(), rc);
        this.nuxeoClient = coreSessionClient;

    }

    public NuxeoPrincipal getPrincipal() {
        if (principal == null) {
            // user().principal().getString("username")
            principal = new NuxeoPrincipalImpl("admin", "tenants");
            principal.setAdministrator(true);
        }
        return principal;
    }

    public void session(Handler<AsyncResult<CloseableCoreSession>> resultHandler) {
        nuxeoClient.session(getPrincipal(), h -> {
            CloseableCoreSession session = h.result();
            resultHandler.handle(Future.succeededFuture(session));
            session.save();
        });
        nuxeoClient.close();
    }

    public void query(String query, JsonObject options, Handler<AsyncResult<List<DocumentModel>>> resultHandler) {
        nuxeoClient.session(getPrincipal(), h -> {
            CloseableCoreSession session = h.result();
            resultHandler.handle(Future.succeededFuture(session.query(query)));
            session.save();
        });
        nuxeoClient.close();
    }

}
