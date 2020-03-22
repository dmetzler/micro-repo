package org.nuxeo.micro.repo.service.schema;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface SchemaService {

    /**
     * The address on which the service is published.
     */
    String ADDRESS = "service.nuxeo.schema";

    /**
     * The address on which the successful action are sent.
     */
    String EVENT_ADDRESS = "nuxeo.schema";

    String NUXEO_TENANTS_SCHEMA = "__nuxeotenants";

    /**
     * Create a Nuxeo Client which shares its data source with any other Nuxeo
     * clients created with the same tenant.
     *
     * @param vertx    the Vert.x instance
     * @param tenantId the pool name
     * @param config   the configuration
     * @return the client
     */
    static void create(Vertx vertx, JsonObject config, Handler<AsyncResult<SchemaService>> completionHandler) {
        vertx.executeBlocking(future -> {
            SchemaService result = new SchemaServiceImpl(vertx, config);
            future.complete(result);
        }, completionHandler);

    }


    void getSchema(String tenantId, Handler<AsyncResult<RemoteSchemaManager>> resultHandler);


    static SchemaService createProxy(Vertx vertx, String nuxeoTenantsSchema) {
        return new SchemaServiceVertxEBProxy(vertx, ADDRESS);
    }
}
