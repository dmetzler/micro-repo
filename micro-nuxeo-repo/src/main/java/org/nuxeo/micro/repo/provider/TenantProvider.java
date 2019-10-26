package org.nuxeo.micro.repo.provider;

public interface TenantProvider<T> {

    T getForTenant(String tenantId);
}
