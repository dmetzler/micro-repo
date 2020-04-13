package org.nuxeo.micro.repo.service.dsl.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.micro.repo.service.dsl.NuxeoDslService;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class DslVerticleTest {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        vertx.deployVerticle(new DslVerticle(), testContext.succeeding(id -> testContext.completeNow()));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void can_use_create_document(Vertx vertx, VertxTestContext testContext) throws Throwable {
        NuxeoDslService dslService = NuxeoDslService.createProxy(vertx);

        assertThat(dslService).isNotNull();

        dslService.getAbstracSyntaxTree(
                "doctype NewType { schemas { common dublincore custom { one two }} facets {Folderish}}",
                testContext.succeeding(ast -> {

                    assertThat(((List) ast.get("doctypes"))).hasSize(1);
                    testContext.completeNow();

                }));

    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException {
        testContext.completeNow();
    }

}
