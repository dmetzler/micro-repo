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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.repo.service.core.CoreSessionService;
import org.nuxeo.micro.repo.service.core.CoreVerticle;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class CoreServiceTest {

    private File appConfigFile;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {

        String applicationConfigYaml = "port: 8080\n";

        appConfigFile = File.createTempFile("appconfig-", ".yaml");
        FileWriter writer = new FileWriter(appConfigFile);
        writer.write(applicationConfigYaml);
        writer.close();
        appConfigFile.deleteOnExit();
        vertx.deployVerticle(new CoreVerticle(), testContext.succeeding(id -> testContext.completeNow()));

    }

    @Test
    void can_use_create_document(Vertx vertx, VertxTestContext testContext) throws Throwable {

        CoreSessionService.create(vertx, null, testContext.succeeding(css -> {

            css.session(SchemaService.NUXEO_TENANTS_SCHEMA, "Administrator",
                    testContext.succeeding(session -> testContext.verify(() -> {

                        DocumentModel doc = session.createDocumentModel("/", "Tenants", "Folder");
                        assertThat(doc).isNotNull();
                        doc.setPropertyValue("dc:title", "Tenants");
                        doc = session.createDocument(doc);

                        try {
                            doc = session.getDocument(new PathRef("/Tenants"));
                        } catch (DocumentNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        assertThat(doc).isNotNull();
                        testContext.completeNow();

                    })));

        }));

    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
