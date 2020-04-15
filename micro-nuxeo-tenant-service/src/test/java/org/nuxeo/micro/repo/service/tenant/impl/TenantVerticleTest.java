package org.nuxeo.micro.repo.service.tenant.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.micro.repo.service.tenant.TenantService;
import org.nuxeo.micro.repo.service.tenant.impl.TenantVerticle;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class TenantVerticleTest {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        vertx.deployVerticle(new TenantVerticle(), testContext.succeeding(id -> testContext.completeNow()));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void can_use_create_document(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TenantService tenantService = TenantService.createProxy(vertx);

        assertThat(tenantService).isNotNull();

        tenantService.getTenantConfiguration("test", testContext.failing(v -> testContext.verify(() -> {
            testContext.completeNow();
        })));

    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
