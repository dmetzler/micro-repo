package org.nuxeo.micro.repo.service.schema.impl;

import org.nuxeo.micro.repo.service.BaseVerticle;
import org.nuxeo.micro.repo.service.schema.SchemaService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.serviceproxy.ServiceBinder;

public class SchemaVerticle extends BaseVerticle {
    private static final Logger log = LoggerFactory.getLogger(SchemaVerticle.class);

    private static final Integer DEFAULT_PORT = 8080;

    private MessageConsumer<JsonObject> consumer;

    private ServiceBinder binder;

    public static void main(String[] args) {
        ClusterManager mgr = new InfinispanClusterManager();

        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                vertx.deployVerticle(new SchemaVerticle());
            } else {
                // failed!
            }
        });
    }


    @Override
    public void stop(Promise<Void> stopFuture) throws Exception {
        if (binder != null && consumer != null) {
            binder.unregister(consumer);
        }
        stopFuture.complete();
    }

    @Override
    public void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler) {
        binder = new ServiceBinder(vertx);
        SchemaService.create(vertx, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                SchemaService ss = ar.result();

                consumer = binder.setAddress(SchemaService.ADDRESS).register(SchemaService.class, ss);

                log.info("Nuxeo Schema Service  published");

                // Used for health check
                vertx.createHttpServer()
                     .requestHandler(req -> req.response().end("OK"))
                     .listen(config.getInteger("port", DEFAULT_PORT));
                completionHandler.handle(Future.succeededFuture());

            } else {
                completionHandler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }

}
