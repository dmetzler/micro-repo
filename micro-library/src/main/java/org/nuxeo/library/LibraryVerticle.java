package org.nuxeo.library;

import org.nuxeo.library.model.LibraryOperations;
import org.nuxeo.vertx.Nuxicle;
import org.nuxeo.vertx.graphql.NuxeoGQLConfiguration;

import graphql.GraphQL;
import io.vertx.core.Vertx;

public class LibraryVerticle extends Nuxicle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new LibraryVerticle());
    }

    @Override
    protected GraphQL configureGraphQL() {
        return NuxeoGQLConfiguration.builder()//
                .configuration(LibraryOperations.class)//
                .build(vertx);
    }

}
