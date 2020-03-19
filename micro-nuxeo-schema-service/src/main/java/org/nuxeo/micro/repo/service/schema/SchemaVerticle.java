package org.nuxeo.micro.repo.service.schema;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

public class SchemaVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(SchemaVerticle.class);
    private MessageConsumer<JsonObject> consumer;
    private ServiceBinder binder;


    @Override
    public void start(Promise<Void> startFuture) throws Exception {

        binder = new ServiceBinder(vertx);
        SchemaService.create(vertx, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                SchemaService ss = ar.result();

                consumer = binder.setAddress(SchemaService.ADDRESS).register(SchemaService.class, ss);

                log.info("Nuxeo Schema Service  published");

                // Used for health check
                vertx.createHttpServer().requestHandler(req -> req.response().end("OK")).listen(8080);
                startFuture.complete();

            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopFuture) throws Exception {
        binder.unregister(consumer);
        stopFuture.complete();
    }

}
