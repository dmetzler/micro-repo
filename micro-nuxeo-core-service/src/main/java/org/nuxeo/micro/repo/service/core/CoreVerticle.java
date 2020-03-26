package org.nuxeo.micro.repo.service.core;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxImplBase;
import org.nuxeo.micro.repo.service.core.impl.GrpcInterceptor;
import org.nuxeo.micro.repo.service.core.impl.NuxeoCoreSessionGrpcImpl;
import org.nuxeo.runtime.jtajca.JtaActivator;

import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.grpc.BlockingServerInterceptor;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class CoreVerticle extends AbstractVerticle {
    static final int DEFAULT_PORT = 8787;
    private static final Logger log = LoggerFactory.getLogger(CoreVerticle.class);
    private VertxServer rpcServer;
    private ConfigStoreOptions configOptions;

    private JtaActivator jta;

    public CoreVerticle() {
        this.configOptions = getConfigDefaultStoreOptions();
    }

    public CoreVerticle(ConfigStoreOptions configOptions) {
        this.configOptions = configOptions;

    }

    protected ConfigStoreOptions getConfigDefaultStoreOptions() {
        return new ConfigStoreOptions().setType("file")//
                .setFormat("yaml")//
                .setOptional(true)//
                .setConfig(new JsonObject()//
                        .put("path", "config/application.yaml"));
    }

    @Override
    public void start(Promise<Void> startFuture) throws Exception {

        StopWatch watch = new StopWatch();
        watch.start();

        // Activate JTA
        jta = new JtaActivator();
        jta.activate();

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(configOptions);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig(ch -> {
            if (ch.succeeded()) {

                JsonObject config = ch.result();
                NuxeoCoreSessionGrpcImpl.create(vertx, config, sh -> {
                    if (sh.succeeded()) {
                        try {
                            NuxeoCoreSessionVertxImplBase service = sh.result();

                            GrpcInterceptor interceptor = new GrpcInterceptor();
                            ServerInterceptor wrapped = BlockingServerInterceptor.wrap(vertx, interceptor);

                            rpcServer = VertxServerBuilder
                                    .forAddress(vertx, "0.0.0.0", config.getInteger("port", DEFAULT_PORT))
                                    .addService(ServerInterceptors.intercept(service, wrapped)).build();
                            rpcServer.start();
                            watch.stop();
                            log.info("Started Nuxeo Core Verticle in {}ms", watch.getTime());
                            startFuture.complete();
                        } catch (IOException e) {
                            startFuture.fail(e);
                        }
                    } else {
                        startFuture.fail(sh.cause());
                    }
                });

            } else {
                startFuture.fail(ch.cause());
            }

        });

    }

    @Override
    public void stop(Promise<Void> stopFuture) throws Exception {
        jta.deactivate();
        rpcServer.shutdown(h -> stopFuture.complete());

    }

}
