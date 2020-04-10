package org.nuxeo.micro.repo.service.graphql;

public class Hello {

    private String tenantId;

    public Hello(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMessage() {
        return String.format("Hello %s!", tenantId);
    }
}
