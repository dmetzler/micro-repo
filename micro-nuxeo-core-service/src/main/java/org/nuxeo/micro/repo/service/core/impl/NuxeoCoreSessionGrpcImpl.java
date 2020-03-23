package org.nuxeo.micro.repo.service.core.impl;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.micro.repo.service.core.CoreSessionService;
import org.nuxeo.micro.repo.service.grpc.Document;
import org.nuxeo.micro.repo.service.grpc.DocumentCreationRequest;
import org.nuxeo.micro.repo.service.grpc.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.service.schema.SchemaService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class NuxeoCoreSessionGrpcImpl extends NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxImplBase {

    private Vertx vertx;
    private JsonObject config;
    private CoreSessionService coreSessionService;

    public NuxeoCoreSessionGrpcImpl(Vertx vertx, JsonObject config, CoreSessionService coreSessionService) {
        this.vertx = vertx;
        this.config = config;
        this.coreSessionService = coreSessionService;
    }

    public static void create(Vertx vertx, JsonObject config,
            Handler<AsyncResult<NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxImplBase>> completionHandler) {
        CoreSessionService.create(vertx, config, ar -> {
            if (ar.succeeded()) {
                NuxeoCoreSessionGrpcImpl server = new NuxeoCoreSessionGrpcImpl(vertx, config, ar.result());
                completionHandler.handle(Future.succeededFuture(server));
            } else {
                completionHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void createDocument(DocumentCreationRequest request, Promise<Document> response) {
        coreSessionService.session(SchemaService.NUXEO_TENANTS_SCHEMA, "dmetzler@nuxeo.com", sh -> {
            if (sh.failed()) {
                response.fail(sh.cause());
            } else {
                CoreSession session = sh.result();
                DocumentModel doc = session.createDocumentModel(request.getPath(), request.getName(),
                        request.getType());
                // TODO: map a Document from the document creation request

                doc = session.createDocument(doc);

                Document result = Document.newBuilder().setName(doc.getName()).setUuid(doc.getId())
                        .setPath(doc.getPathAsString()).setType(doc.getType()).build();

                response.complete(result);

            }
        });
    }

}
