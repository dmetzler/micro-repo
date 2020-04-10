package org.nuxeo.micro.repo.service.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentCreationRequest;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.QueryRequest;
import org.nuxeo.micro.repo.proto.utils.DocumentBuilder;
import org.nuxeo.micro.repo.service.core.impl.GrpcInterceptor;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.micro.repo.service.schema.impl.SchemaVerticle;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class GrpcServerTest {

    private NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub nuxeoSession;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        JsonObject config = new JsonObject().put("db", new JsonObject().put("server", "localhost:27017"));

        ConfigStoreOptions json = new ConfigStoreOptions().setType("json").setConfig(config);

        vertx.deployVerticle(new SchemaVerticle(), testContext.succeeding(ar -> {
            vertx.deployVerticle(new CoreVerticle(json), testContext.succeeding(id -> {
                ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, "localhost", CoreVerticle.DEFAULT_PORT)
                        .usePlaintext(true).build();

                Metadata headers = new Metadata();
                headers.put(GrpcInterceptor.TENANTID_METADATA_KEY, SchemaService.NUXEO_TENANTS_SCHEMA);
                nuxeoSession = NuxeoCoreSessionGrpc.newVertxStub(channel)
                        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
                testContext.completeNow();
            }));

        }));

    }

    @Test
    void can_create_a_document(Vertx vertx, VertxTestContext testContext) throws Exception {

        DocumentCreationRequest req = documentCreationRequest("/", "Test", "Folder").build();

        nuxeoSession.createDocument(req, testContext.succeeding(resp -> testContext.verify(() -> {
            assertThat(resp.getType()).isEqualTo("Folder");
            assertThat(resp.getUuid()).isNotNull();
            assertThat(resp.getPropertiesMap()).isNotNull();
            assertThat(resp.getPropertiesMap().get("dc:title")).isNotNull();
            assertThat(resp.getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue()).isEqualTo("Test");

            String id = resp.getUuid();

            nuxeoSession.getDocument(DocumentRequest.newBuilder().setId(id).build(),
                    testContext.succeeding(protoDoc -> testContext.verify(() -> {
                        assertThat(protoDoc.getType()).isEqualTo("Folder");
                        assertThat(protoDoc.getUuid()).isNotNull();
                        assertThat(protoDoc.getPropertiesMap()).isNotNull();
                        assertThat(protoDoc.getPropertiesMap().get("dc:title")).isNotNull();
                        assertThat(protoDoc.getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue())
                                .isEqualTo("Test");

                        testContext.completeNow();
                    })));

        })));
    }

    private DocumentCreationRequest.Builder documentCreationRequest(String path, String name, String type) {
        Document doc = DocumentBuilder.create(type).setPropertyValue("dc:title", name).build();
        return DocumentCreationRequest.newBuilder().setPath("/").setName(name).setDocument(doc);
    }

    @Test
    void can_update_a_document(Vertx vertx, VertxTestContext testContext) throws Exception {

        DocumentCreationRequest req = documentCreationRequest("/", "Test", "Folder").build();

        nuxeoSession.createDocument(req, testContext.succeeding(resp -> testContext.verify(() -> {

            Document.Property title = Document.Property.newBuilder()
                    .addScalarValue(Document.Property.ScalarProperty.newBuilder().setStrValue("Test2")).build();
            Document doc = Document.newBuilder(resp).putProperties("dc:title", title).build();

            nuxeoSession.updateDocument(doc, testContext.succeeding(protoDoc -> testContext.verify(() -> {

                assertThat(protoDoc.getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue())
                        .isEqualTo("Test2");

                nuxeoSession.getDocument(DocumentRequest.newBuilder().setId(doc.getUuid()).build(),
                        testContext.succeeding(updatedDoc -> testContext.verify(() -> {

                            assertThat(updatedDoc.getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue())
                                    .isEqualTo("Test2");

                            testContext.completeNow();
                        })));
            })));

        })));
    }

    @Test
    void can_delete_a_document(Vertx vertx, VertxTestContext testContext) throws Exception {

        DocumentCreationRequest req = documentCreationRequest("/", "Test", "Folder").build();

        nuxeoSession.createDocument(req, testContext.succeeding(resp -> testContext.verify(() -> {

            nuxeoSession.deleteDocument(resp, testContext.succeeding(protoDoc -> testContext.verify(() -> {

                nuxeoSession.getDocument(DocumentRequest.newBuilder().setId(resp.getUuid()).build(),
                        testContext.failing(t -> testContext.verify(() -> {
                            testContext.completeNow();
                        })));
            })));

        })));
    }

    @Test
    void can_query_documents(Vertx vertx, VertxTestContext testContext) throws Exception {

        DocumentCreationRequest req = documentCreationRequest("/", "Test", "Folder").build();

        nuxeoSession.createDocument(req, testContext.succeeding(r -> testContext.verify(() -> {

            QueryRequest query = QueryRequest.newBuilder().setNxql("SELECT * FROM Folder WHERE dc:title = 'Test'")
                    .build();

            nuxeoSession.query(query, testContext.succeeding(resp -> testContext.verify(() -> {

                List<Document> docs = resp.getDocsList();
                assertThat(docs).hasSize(1);
                assertThat(docs.get(0).getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue())
                        .isEqualTo("Test");

            })));
        })));
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
