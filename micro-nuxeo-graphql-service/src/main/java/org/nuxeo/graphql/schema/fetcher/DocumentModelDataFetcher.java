package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.NuxeoContext;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;

public class DocumentModelDataFetcher extends AbstractDataFetcher<Document> {

    @Override
    public void get(DataFetchingEnvironment environment, Promise<Document> future) {
        String path = environment.getArgument("path");
        String id = environment.getArgument("id");

        NuxeoContext ctx = environment.getContext();

        ctx.session(sr -> {
            if (sr.succeeded()) {
                NuxeoCoreSessionVertxStub session = sr.result();
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
                future.fail(sr.cause());
            }
        });

    }

}
