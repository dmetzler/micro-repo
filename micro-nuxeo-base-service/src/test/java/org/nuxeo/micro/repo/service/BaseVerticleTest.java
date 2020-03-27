package org.nuxeo.micro.repo.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class BaseVerticleTest {

    public static class TestVerticle extends BaseVerticle {
        public void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler) {
            completionHandler.handle(Future.succeededFuture());
        }
    }

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        vertx.deployVerticle(new TestVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    void can_use_create_document(Vertx vertx, VertxTestContext testContext) throws Throwable {

        assertNotNull(testContext);
        testContext.completeNow();
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
