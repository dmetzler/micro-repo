package org.nuxeo.micro.repo.provider.impl;

import java.net.URL;

import org.nuxeo.micro.repo.provider.TenantSchemaUrlResolver;

public class ClassPathSchemaUrlResolver implements TenantSchemaUrlResolver {

    private ClassLoader cl;

    public ClassPathSchemaUrlResolver(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    public URL getForTenant(String tenantId) {
        return cl.getResource(tenantId + ".nxl");
    }

}
