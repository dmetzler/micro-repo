package com.nuxeo.cloud.tenants.nuxeo;

import java.util.List;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;

import com.nuxeo.cloud.tenants.nuxeo.impl.CoreSessionClientImpl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * A Vert.x service used to interact with Nuxeo repository
 *
 * @author dmetzler
 *
 */
public interface CoreSessionClient {

    /**
     * Create a Nuxeo Client which shares its data source with any other Nuxeo
     * clients created with the same tenant.
     *
     * @param vertx    the Vert.x instance
     * @param tenantId the pool name
     * @param config   the configuration
     * @return the client
     */
    static void create(Vertx vertx, String tenantId, JsonObject config,
            Handler<AsyncResult<CoreSessionClient>> completionHandler) {
        vertx.executeBlocking(future -> {
            CoreSessionClient result = new CoreSessionClientImpl(vertx, tenantId, config);
            future.complete(result);
        }, completionHandler);

    }

    /**
     * Create a Nuxeo Client which shares its data source with any other Nuxeo
     * clients created with the same tenant.
     *
     * @param vertx
     * @param tenantId
     * @return the client
     */
    static void create(Vertx vertx, String tenantId, Handler<AsyncResult<CoreSessionClient>> completionHandler) {
        create(vertx, tenantId, new JsonObject(), completionHandler);
    }

    /**
     * Queries nuxeo core and return a list of {@link DocumentModel}.
     *
     * @param query         The NXQL query
     * @param principal     The NuxeoPrincipal that runs the query
     * @param options       an object containing options for the query
     * @param resultHandler resultHandler will be provided with the list of
     *                      resulting documents.
     * @return
     */
    @Fluent
    CoreSessionClient query(String query, NuxeoPrincipal principal, JsonObject options,
            Handler<AsyncResult<List<DocumentModel>>> resultHandler);

    /**
     * Queries nuxeo core and return a list of {@link DocumentModel}.
     *
     * @param query         The NXQL query
     * @param principal     The NuxeoPrincipal that runs the query
     * @param options       an object containing options for the query
     * @param resultHandler resultHandler will be provided with the list of
     *                      resulting documents.
     * @return
     */
    @Fluent
    CoreSessionClient session(NuxeoPrincipal principal, Handler<AsyncResult<CloseableCoreSession>> resultHandler);

    /**
     * Get a document by its reference.
     *
     * @param docRef        the reference of the document
     * @param principal     The NuxeoPrincipal that runs the query
     * @param options       an object containing options for the query
     * @param resultHandler resultHandler will be provided with retrieved document
     * @return
     */
    @Fluent
    CoreSessionClient getDocument(DocumentRef docRef, NuxeoPrincipal principal, JsonObject jsonObject,
            Handler<AsyncResult<@Nullable DocumentModel>> resultHandler);

    /**
     * Close the client and release its resources
     */
    public void close();
}
