package org.nuxeo.micro.repo.service.graphql;

import org.nuxeo.micro.repo.service.graphql.impl.GraphQLServiceImpl;

import graphql.GraphQL;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface GraphQLService {

    static void create(Vertx vertx, JsonObject config, Handler<AsyncResult<GraphQLService>> completionHandler) {
        vertx.executeBlocking(future -> {
            GraphQLService result = new GraphQLServiceImpl(vertx, config);
            future.complete(result);
        }, completionHandler);

    }

    void getGraphQL(String tenantId, Handler<AsyncResult<GraphQL>> completionHandler);



}
