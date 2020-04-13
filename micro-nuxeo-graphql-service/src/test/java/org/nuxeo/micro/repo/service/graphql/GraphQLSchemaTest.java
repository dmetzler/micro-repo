/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
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
 *     dmetzler
 */
package org.nuxeo.micro.repo.service.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.repo.service.core.CoreSessionService;
import org.nuxeo.micro.repo.service.core.CoreVerticle;
import org.nuxeo.micro.repo.service.dsl.impl.DslVerticle;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.micro.repo.service.schema.impl.SchemaVerticle;
import org.nuxeo.runtime.transaction.TransactionHelper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class GraphQLSchemaTest {

    String schemaDsl = "doctype Library extends Folder {\n" + "schemas {\n" + "    common dublincore\n"
            + "      lib:library { city, country}\n" + "  }    \n" + "  aliases {\n" + "    name prop {\"dc:title\"}\n"
            + "      city prop {\"lib:city\"}\n" + "      country prop {\"lib:country\"}        \n"
            + "      books query { \"select * from Book where ecm:parentId = '${this.id}'\", \"Book\"}\n" + "  }\n"
            + "  crud\n" + "}";

    private JsonObject coreConfig;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        coreConfig = new JsonObject().put("db", new JsonObject().put("server", "localhost:27017"));

        vertx.deployVerticle(new DslVerticle(), testContext.succeeding(id1 -> {
            vertx.deployVerticle(new SchemaVerticle(), testContext.succeeding(id3 -> {
                vertx.deployVerticle(new CoreVerticle(), testContext.succeeding(id2 -> {
                    vertx.deployVerticle(new GraphQLVerticle(), testContext.succeeding(id4 -> {

                        CoreSessionService.create(vertx, coreConfig, testContext.succeeding(sessionService -> {
                            sessionService.session(SchemaService.NUXEO_TENANTS_SCHEMA, "dmetzler@nuxeo.com",
                                    testContext.succeeding(session -> {
                                        session.removeDocument(new PathRef("/library"));
                                        testContext.completeNow();
                                    }));
                        }));

                    }));
                }));
            }));
        }));

    }

    @Test
    void can_create_new_tenant(Vertx vertx, VertxTestContext testContext) throws Throwable {
        SchemaService schemaService = SchemaService.createProxy(vertx);

        // Given a non existing schema
        schemaService.getSchema("library", testContext.failing(t -> {

            CoreSessionService.create(vertx, coreConfig, testContext.succeeding(sessionService -> {
                sessionService.session(SchemaService.NUXEO_TENANTS_SCHEMA, "dmetzler@nuxeo.com",
                        testContext.succeeding(session -> {

                            // When I create a tenant document
                            DocumentModel doc = session.createDocumentModel("/", "library", "Tenant");
                            doc.setPropertyValue("tenant:schemaDef", schemaDsl);
                            doc = session.createDocument(doc);

                            TransactionHelper.commitOrRollbackTransaction();
                            TransactionHelper.startTransaction();

                            // Then I can retrieve the schema of that tenant
                            schemaService.getSchema("library", testContext.succeeding(schemaManager -> {

                                assertThat(schemaManager.getDocumentType("Library")).isNotNull();

                                sessionService.session("library", "dmetzler@nuxeo.com",
                                        testContext.succeeding(session2 -> {

                                            // Then I can create a Library document
                                            DocumentModel doc2 = session2.createDocumentModel("/", "Test", "Library");
                                            doc2.setPropertyValue("lib:city", "Irvine");
                                            doc2 = session2.createDocument(doc2);

                                            doc2 = session2.getDocument(new PathRef("/Test"));
                                            assertThat(doc2.getPropertyValue("lib:city")).isEqualTo("Irvine");

                                            testContext.completeNow();
                                        }));

                            }));

                        }));
            }));

        }));

    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
