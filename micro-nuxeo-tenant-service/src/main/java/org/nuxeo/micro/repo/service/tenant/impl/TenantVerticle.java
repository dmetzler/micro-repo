package org.nuxeo.micro.repo.service.tenant.impl;

import org.nuxeo.micro.repo.service.BaseVerticle;
import org.nuxeo.micro.repo.service.tenant.TenantService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

public class TenantVerticle extends BaseVerticle {

    private static final Logger log = LoggerFactory.getLogger(TenantVerticle.class);

    private static final Integer DEFAULT_PORT = 8080;

    private MessageConsumer<JsonObject> consumer;

    private ServiceBinder binder;

    @Override
    public void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler) {
        binder = new ServiceBinder(vertx);
        TenantService.create(vertx, config, ar -> {
            if (ar.succeeded()) {
                TenantService ts = ar.result();

                consumer = binder.setAddress(TenantService.ADDRESS).register(TenantService.class, ts);

                log.info("Nuxeo Tenant Service  published");

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

    @Override
    public void stop(Promise<Void> stopFuture) throws Exception {
        if (binder != null && consumer != null) {
            binder.unregister(consumer);
        }
        stopFuture.complete();
    }

}
