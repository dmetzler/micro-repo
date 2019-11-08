package org.nuxeo.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nuxeo.library.model.LibraryOperations;
import org.nuxeo.vertx.graphql.NuxeoGQLConfiguration;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)

public class TestGraphQLSetup {

    @Test
    public void testName() throws Exception {

        Vertx vertx = Vertx.vertx();

        NuxeoGQLConfiguration.builder()//
                .configuration(LibraryOperations.class)//
                .build(vertx);

    }
}
