package org.nuxeo.micro.repo.service.core;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.micro.repo.service.core.impl.CoreSessionServiceImpl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface CoreSessionService {

    /**
     * The address on which the service is published.
     */
    String ADDRESS = "service.nuxeo.coresession";

    /**
     * The address on which the successful action are sent.
     */
    String EVENT_ADDRESS = "nuxeo.coresession";

    static void create(Vertx vertx, JsonObject config, Handler<AsyncResult<CoreSessionService>> completionHandler) {
        vertx.executeBlocking(future -> {
            CoreSessionService result = new CoreSessionServiceImpl(vertx, config);
            future.complete(result);
        }, completionHandler);

    }

    public void session(String tenantId, String username, Handler<AsyncResult<CoreSession>> sessionHandler);

}
