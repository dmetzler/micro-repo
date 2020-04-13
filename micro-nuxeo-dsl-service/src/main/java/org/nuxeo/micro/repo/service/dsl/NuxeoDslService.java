package org.nuxeo.micro.repo.service.dsl;

import org.nuxeo.micro.repo.service.dsl.impl.NuxeoDslServiceImpl;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface NuxeoDslService {

    /**
     * The address on which the service is published.
     */
    String ADDRESS = "service.nuxeo.dsl";

    /**
     * The address on which the successful action are sent.
     */
    String EVENT_ADDRESS = "nuxeo.dsl";

    static void create(Vertx vertx, JsonObject config, Handler<AsyncResult<NuxeoDslService>> completionHandler) {
        vertx.executeBlocking(future -> {
            NuxeoDslService result = new NuxeoDslServiceImpl(vertx, config);
            future.complete(result);
        }, completionHandler);

    }

    static NuxeoDslService createProxy(Vertx vertx) {
        return new NuxeoDslServiceVertxEBProxy(vertx, ADDRESS);
    }

    void getAbstracSyntaxTree(String dsl, Handler<AsyncResult<AbstractSyntaxTree>> completionHandler);

}
