package org.nuxeo.micro.repo.service.schema;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentCreationRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.service.core.CoreVerticle;

import io.grpc.ManagedChannel;
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
                ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, "localhost", 8888).usePlaintext(true)
                        .build();

                nuxeoSession = NuxeoCoreSessionGrpc.newVertxStub(channel);
                testContext.completeNow();
            }));

        }));

    }

    @Test
    void can_start_grpc_server(Vertx vertx, VertxTestContext testContext) throws Exception {

        Document.Property title = Document.Property.newBuilder()
                .addScalarValue(Document.Property.ScalarProperty.newBuilder().setStrValue("Test")).build();

        Document doc = Document.newBuilder().setType("Folder").putProperties("dc:title", title).build();

        DocumentCreationRequest req = DocumentCreationRequest.newBuilder().setPath("/").setName("test").setDocument(doc)
                .build();

        nuxeoSession.createDocument(req, testContext.succeeding(resp -> testContext.verify(() -> {
            assertThat(resp.getType()).isEqualTo("Folder");
            assertThat(resp.getPropertiesMap()).isNotNull();
            assertThat(resp.getPropertiesMap().get("dc:title")).isNotNull();
            assertThat(resp.getPropertiesMap().get("dc:title").getScalarValue(0).getStrValue()).isEqualTo("Test");
            testContext.completeNow();
        })));
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
