package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;

public class DocumentModelDataFetcher extends AbstractDataFetcher {

    public void get(DataFetchingEnvironment environment, Promise<Document> future) {
        String path = environment.getArgument("path");
        String id = environment.getArgument("id");

        NuxeoCoreSessionVertxStub session = getSession(environment.getContext());
        if (session != null) {

            DocumentRequest req = null;

            if (path != null) {
                req = DocumentRequest.newBuilder().setPath(path).build();
            }
            if (id != null) {
                req = DocumentRequest.newBuilder().setId(id).build();
            }

            if (req != null) {

                session.getDocument(req, dr -> {
                    if (dr.succeeded()) {
                        future.complete(dr.result());
                    } else {
                        future.fail(dr.cause());
                    }
                });

            } else {
                future.fail("A document request needs either an id or a path");
            }

        } else {

            future.fail("Unable to get a session to Core");
        }
    }

}
