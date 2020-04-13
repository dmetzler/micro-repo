package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class DocumentModelDataFetcher extends AbstractDataFetcher implements DataFetcher<Document>{

    @Override
    public Document get(DataFetchingEnvironment environment) {
        String path = environment.getArgument("path");
        String id = environment.getArgument("id");

        NuxeoCoreSessionVertxStub session = getSession(environment.getContext());
        if (session != null) {
            if (path != null) {
                return session.getDocument(new PathRef(path));
            }
            if (id != null) {
                return session.getDocument(new IdRef(id));
            }
        }
        return null;
    }

}
