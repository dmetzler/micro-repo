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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.utils.GrpcInterceptor;
import org.nuxeo.micro.repo.service.core.CoreSessionService;
import org.nuxeo.micro.repo.service.core.CoreVerticle;
import org.nuxeo.micro.repo.service.dsl.impl.DslVerticle;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.micro.repo.service.schema.impl.SchemaVerticle;

import com.google.common.base.Joiner;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class GraphqlServiceTest {

    /**
     *
     */
    private static final String TENANT_ID = "library";

    String schemaDsl;

    private JsonObject coreConfig;

    private GraphQLService gqlService;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {

        File dslFile = new File(this.getClass().getResource("/library.nxl").getFile());
        schemaDsl = new String(Files.readAllBytes(Paths.get(dslFile.getAbsolutePath())));

        coreConfig = new JsonObject().put("db", new JsonObject().put("server", "localhost:27017"));

        vertx.deployVerticle(new DslVerticle(), testContext.succeeding(id1 -> {
            vertx.deployVerticle(new SchemaVerticle(), testContext.succeeding(id3 -> {
                vertx.deployVerticle(new CoreVerticle(), testContext.succeeding(id2 -> {
                    vertx.deployVerticle(new GraphQLVerticle(), testContext.succeeding(id4 -> {

                        CoreSessionService.create(vertx, coreConfig, testContext.succeeding(sessionService -> {
                            sessionService.session(SchemaService.NUXEO_TENANTS_SCHEMA, "dmetzler@nuxeo.com",
                                    testContext.succeeding(session -> {
                                        setupTenant(testContext, sessionService, session);

                                        GraphQLService.create(vertx, coreConfig, testContext.succeeding(gqlService -> {
                                            this.gqlService = gqlService;
                                            testContext.completeNow();
                                        }));

                                    }));
                        }));

                    }));
                }));
            }));
        }));

    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
    void can_create_new_tenant(Vertx vertx, VertxTestContext testContext) throws Throwable {

        executeQuery(vertx, " {document(path:\"/Test\") { _id _path }}", testContext.succeeding(result -> testContext.verify(()->{
            if (result.getErrors().size() > 0) {
                throw new NuxeoException(Joiner.on(", ").join(result.getErrors()));
            }

            Map<String, Object> queryResult = result.getData();

            System.out.println(result);
            Map<String, Object> document = (Map<String, Object>) queryResult.get("document");
            assertThat(document.get("_id")).isNotNull();
            testContext.completeNow();
        })));

    }

    private void executeQuery(Vertx vertx, final String query,
            Handler<AsyncResult<ExecutionResult>> completionHandler) {

        gqlService.getGraphQL(TENANT_ID, graphQLR -> {
            if (graphQLR.succeeded()) {
                GraphQL graphQL = graphQLR.result();

                RoutingContext routingContext = mockRoutinContext("/");


                ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, "localhost", 8787)
                                                            .usePlaintext(true)
                                                            .build();

                Metadata headers = new Metadata();
                headers.put(GrpcInterceptor.TENANTID_METADATA_KEY, TENANT_ID);

                NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub nuxeoSession = NuxeoCoreSessionGrpc.newVertxStub(
                        channel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

                vertx.executeBlocking(future -> {
                    try {
                        ExecutionResult result = graphQL.execute(query, new NuxeoContext(routingContext, nuxeoSession));
                        future.complete(result);
                    } catch (Exception e) {
                        future.fail(e);
                    }

                }, v -> {
                    if (v.succeeded()) {
                        completionHandler.handle(Future.succeededFuture((ExecutionResult) v.result()));
                    } else {
                        completionHandler.handle(Future.failedFuture(v.cause()));
                    }
                });
            } else {
                completionHandler.handle(Future.failedFuture(graphQLR.cause()));
            }
        });

    }

    private void setupTenant(VertxTestContext testContext, CoreSessionService sessionService, CoreSession session) {
        session.removeDocument(new PathRef("/library"));

        DocumentModel doc = session.createDocumentModel("/", TENANT_ID, "Tenant");
        doc.setPropertyValue("tenant:schemaDef", schemaDsl);
        doc = session.createDocument(doc);

        sessionService.session(TENANT_ID, "dmetzler@nuxeo.com", testContext.succeeding(librarySession -> {

            DocumentModel doc2 = librarySession.createDocumentModel("/", "Test", "Library");
            doc2.setPropertyValue("lib:city", "Irvine");
            doc2 = librarySession.createDocument(doc2);

            doc2 = librarySession.getDocument(new PathRef("/Test"));
            assertThat(doc2.getPropertyValue("lib:city")).isEqualTo("Irvine");
        }));
    }

    private RoutingContext mockRoutinContext(String path) {
        RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.normalisedPath()).thenReturn(path);
        Route currentRoute = mock(Route.class);
        when(currentRoute.getPath()).thenReturn(path);
        when(routingContext.currentRoute()).thenReturn(currentRoute);
        return routingContext;
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
