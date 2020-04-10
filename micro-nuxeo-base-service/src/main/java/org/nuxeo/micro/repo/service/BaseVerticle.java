package org.nuxeo.micro.repo.service;

import org.apache.commons.lang3.time.StopWatch;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public abstract class BaseVerticle extends AbstractVerticle {

    protected static final Logger log = LoggerFactory.getLogger(BaseVerticle.class);
    private ConfigStoreOptions configOptions;

    public BaseVerticle() {
        this.configOptions = getConfigDefaultStoreOptions();
    }

    public BaseVerticle(ConfigStoreOptions configOptions) {
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

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(configOptions);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig(ch -> {
            if (ch.succeeded()) {
                startWithConfig(ch.result(), ar -> {
                    if (ar.succeeded()) {
                        watch.stop();
                        log.info("Started in {}ms", watch.getTime());
                        startFuture.complete();
                    } else {
                        startFuture.fail(ar.cause());
                    }
                });
            } else {
                startFuture.fail(ch.cause());
            }

        });

    }

    public abstract void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler);

}