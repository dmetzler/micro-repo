package org.nuxeo.micro.repo.service.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.nuxeo.micro.repo.service.schema.impl.SchemaServiceImpl;
import org.nuxeo.micro.repo.service.tenant.TenantService;

import io.vertx.core.json.JsonObject;

public class RemoteSchemaManagerTest {

    @Test
    public void can_serialize_schemaManager() throws Exception {
        SchemaServiceImpl ss = new SchemaServiceImpl(null, null);

        ss.getSchema(TenantService.NUXEO_TENANTS_SCHEMA, ar -> {
            RemoteSchemaManager rsm = ar.result();
            assertThat(rsm.getDocumentType("Workspace")).isNotNull();

            JsonObject rsmJson = rsm.toJson();

            rsm = new RemoteSchemaManager(rsmJson);
            assertThat(rsm.getDocumentType("Workspace")).isNotNull();

        });
    }
}
