package org.nuxeo.micro.repo.service.core;

import org.nuxeo.micro.repo.service.grpc.Document;
import org.nuxeo.micro.repo.service.grpc.DocumentRequest;
import org.nuxeo.micro.repo.service.grpc.SessionGrpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.serviceproxy.ServiceBinder;

public class CoreVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(CoreVerticle.class);
    private MessageConsumer<JsonObject> consumer;
    private ServiceBinder binder;


    @Override
    public void start(Promise<Void> startFuture) throws Exception {


        SessionGrpc.SessionVertxImplBase service = new SessionGrpc.SessionVertxImplBase() {

            @Override
            public void getDocument(DocumentRequest request, Promise<Document> response) {
                // TODO Auto-generated method stub
                super.getDocument(request, response);
            }
        };

        VertxServer rpcServer = VertxServerBuilder
                  .forAddress(vertx, "0.0.0.0", 8080)
                  .addService(service)
                  .build();

        rpcServer.start();


        binder = new ServiceBinder(vertx);
        CoreSessionService.create(vertx, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                CoreSessionService css = ar.result();



                log.info("Nuxeo Core Session Service  published");

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
