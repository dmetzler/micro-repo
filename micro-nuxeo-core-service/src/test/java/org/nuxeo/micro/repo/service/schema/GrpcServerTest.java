package org.nuxeo.micro.repo.service.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.micro.repo.service.core.CoreVerticle;
import org.nuxeo.micro.repo.service.grpc.Document;
import org.nuxeo.micro.repo.service.grpc.DocumentCreationRequest;
import org.nuxeo.micro.repo.service.grpc.NuxeoCoreSessionGrpc;

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

        DocumentCreationRequest req = DocumentCreationRequest.newBuilder().setPath("/").setName("test")
                .setType("Folder").build();

        Document.DataModel.Property title = Document.DataModel.Property.newBuilder().addScalarValue(Document.DataModel.Property.ScalarProperty.newBuilder().setStrValue("Test")).build();
        Document.DataModel dm = Document.DataModel.newBuilder().setName("dc").putProperties("title", title).build();
        Document.newBuilder().putDatamodel("dc", dm);

        nuxeoSession.createDocument(req, testContext.succeeding(resp -> testContext.verify(()-> {

            assertThat(resp.getType()).isEqualTo("Folder");
            assertThat(resp.getDatamodelMap()).isNotNull();
            assertThat(resp.getDatamodelMap().get("dc")).isNotNull();
            assertThat(resp.getDatamodelMap().get("dc").getPropertiesMap().get("title").getScalarValue(0).getStrValue()).isEqualTo("Test");
            testContext.completeNow();
        })));
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
