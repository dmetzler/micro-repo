package org.nuxeo.micro.repo.service.graphql.model;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.utils.DocumentBuilder;

public class Tenant {

    private String id;
    private String path;
    private String name;
    private String schemaDef;

    private Tenant() {
        // Use builder instead.
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getSchemaDef() {
        return schemaDef;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Tenant tenant) {
        return builder().id(tenant.getId()).path(tenant.getPath()).name(tenant.getName())
                .schemaDef(tenant.getSchemaDef());
    }

    public static class Builder {

        private String id;
        private String path;
        private String name;
        private String schemaDef;

        public Tenant build() {
            Tenant tenant = new Tenant();

            tenant.id = id;
            tenant.path = path;
            tenant.name = name;
            tenant.schemaDef = schemaDef;
            return tenant;
        }

        public Builder schemaDef(String schemaDef) {
            this.schemaDef = schemaDef;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

    }

    public Document toDocument() {
        DocumentBuilder builder = DocumentBuilder.create("Tenant").setPropertyValue("dc:title", name)
                .setPropertyValue("tenant:schemaDef", schemaDef);
        if(StringUtils.isNotBlank(id)) {
        	builder.setId(id);
        }
        return builder.build();
    }

    public static Tenant from(Document result) {
        return builder().id(result.getUuid()).name(result.getName()).path(result.getParentPath())
                .schemaDef(result.getPropertiesMap().get("tenant:schemaDef").getScalarValue(0).getStrValue()).build();
    }

}
