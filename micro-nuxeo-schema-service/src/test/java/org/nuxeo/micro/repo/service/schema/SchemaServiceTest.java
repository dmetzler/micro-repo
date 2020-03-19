/*
 * (C) Copyright 2006-2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Damien Metzler
 */
package org.nuxeo.micro.repo.service.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;

@ExtendWith(VertxExtension.class)
public class SchemaServiceTest {

    private File appConfigFile;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {

        String applicationConfigYaml = "port: 8080\n";

        appConfigFile = File.createTempFile("appconfig-", ".yaml");
        FileWriter writer = new FileWriter(appConfigFile);
        writer.write(applicationConfigYaml);
        writer.close();
        appConfigFile.deleteOnExit();
        vertx.deployVerticle(new SchemaVerticle(),
                testContext.succeeding(id -> testContext.completeNow()));

    }

    @Test
    void can_use_schema_service(Vertx vertx, VertxTestContext testContext) throws Throwable {

        SchemaService.create(vertx, null, ar -> {
            if (ar.succeeded()) {
                SchemaService ss = ar.result();

                ss.getSchema(SchemaServiceImpl.SCHEMA, sar -> {
                    if (sar.succeeded()) {
                        assertThat(sar.result().getDocumentType("Workspace")).isNotNull();
                        testContext.completeNow();
                    } else {
                        testContext.failNow(sar.cause());
                    }
                });
            }
        });

    }

    @Test
    void can_proxy_schema_service(Vertx vertx, VertxTestContext testContext) throws Throwable {

        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(SchemaService.ADDRESS);

        SchemaService ss = builder.build(SchemaService.class);
        // or with delivery options:
        ss.getSchema(SchemaServiceImpl.SCHEMA, ar -> {
            if (ar.succeeded()) {
                assertThat(ar.result().getDocumentType("Workspace")).isNotNull();
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });

    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
