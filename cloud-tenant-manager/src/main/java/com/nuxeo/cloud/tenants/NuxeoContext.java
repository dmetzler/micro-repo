package com.nuxeo.cloud.tenants;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.micro.NuxeoPrincipalImpl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

public class NuxeoContext extends RoutingContextDecorator {

    private NuxeoPrincipalImpl principal;

    public NuxeoContext(RoutingContext rc) {
        super(rc.currentRoute(), rc);
    }

    public NuxeoPrincipal getPrincipal() {
        if (principal == null) {
            // user().principal().getString("username")
            principal = new NuxeoPrincipalImpl("admin", "tenants");
            principal.setAdministrator(true);
        }
        return principal;
    }

}
