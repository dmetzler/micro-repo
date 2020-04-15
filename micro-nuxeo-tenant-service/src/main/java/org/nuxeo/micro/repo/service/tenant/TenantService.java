package org.nuxeo.micro.repo.service.tenant;

import org.nuxeo.micro.repo.service.tenant.impl.TenantServiceImpl;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface TenantService {

    /**
     * The address on which the service is published.
     */
    String ADDRESS = "service.nuxeo.tenant";

    /**
     * The address on which the successful action are sent.
     */
    String EVENT_ADDRESS = "nuxeo.tenant";

    String NUXEO_TENANTS_SCHEMA = "nuxeotenants";

    static void create(Vertx vertx, JsonObject config, Handler<AsyncResult<TenantService>> completionHandler) {
        vertx.executeBlocking(future -> {
            TenantService result = new TenantServiceImpl(vertx, config);
            future.complete(result);
        }, completionHandler);

    }

    static TenantService createProxy(Vertx vertx) {
        return new TenantServiceVertxEBProxy(vertx, ADDRESS);
    }

    void getTenantConfiguration(String tenantId, Handler<AsyncResult<TenantConfiguration>> completionHandler);

}
