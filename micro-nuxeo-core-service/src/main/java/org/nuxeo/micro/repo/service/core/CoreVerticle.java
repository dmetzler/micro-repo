package org.nuxeo.micro.repo.service.core;

import java.io.IOException;

import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxImplBase;
import org.nuxeo.micro.repo.service.BaseVerticle;
import org.nuxeo.micro.repo.service.core.impl.GrpcInterceptor;
import org.nuxeo.micro.repo.service.core.impl.NuxeoCoreSessionGrpcImpl;
import org.nuxeo.runtime.jtajca.JtaActivator;

import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.BlockingServerInterceptor;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class CoreVerticle extends BaseVerticle {
    static final int DEFAULT_PORT = 8787;
    private VertxServer rpcServer;

    private JtaActivator jta;

    public CoreVerticle(ConfigStoreOptions config) {
        super(config);
    }

    @Override
    public void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler) {
        // Activate JTA
        jta = new JtaActivator();
        jta.activate();

        NuxeoCoreSessionGrpcImpl.create(vertx, config, sh -> {
            if (sh.succeeded()) {
                try {
                    NuxeoCoreSessionVertxImplBase service = sh.result();

                    GrpcInterceptor interceptor = new GrpcInterceptor();
                    ServerInterceptor wrapped = BlockingServerInterceptor.wrap(vertx, interceptor);

                    rpcServer = VertxServerBuilder.forAddress(vertx, "0.0.0.0", config.getInteger("port", DEFAULT_PORT))
                            .addService(ServerInterceptors.intercept(service, wrapped)).build();
                    rpcServer.start();

                    completionHandler.handle(Future.succeededFuture());
                } catch (IOException e) {
                    completionHandler.handle(Future.failedFuture(e));
                }
            } else {
                completionHandler.handle(Future.failedFuture(sh.cause()));
            }
        });

    }

    @Override
    public void stop(Promise<Void> stopFuture) throws Exception {
        jta.deactivate();
        rpcServer.shutdown(h -> stopFuture.complete());

    }

}
