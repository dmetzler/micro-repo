package org.nuxeo.graphql.schema.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.el.ELContext;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.el.ELService;
import org.nuxeo.ecm.platform.el.ELServiceServiceImpl;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class NxqlQueryDataFetcher extends AbstractDataFetcher implements DataFetcher<List<Document>> {

    @Override
    public List<Document> get(DataFetchingEnvironment environment) {
        NuxeoCoreSessionVertxStub session = getSession(environment.getContext());
        if (session == null) {
            return null;
        }

        ExpressionEvaluator el = getEl(environment.getContext());
        String finalQuery = getQuery(environment);
        ELService elService = new ELServiceServiceImpl();
        ELContext elContext = elService.createELContext();
        el.bindValue(elContext, "principal", getPrincipal(environment.getContext()));

        if (environment.getArguments().size() > 0) {
            for (Entry<String, Object> paramEntry : environment.getArguments().entrySet()) {
                el.bindValue(elContext, paramEntry.getKey(), paramEntry.getValue());
            }
        }

        if (environment.getSource() instanceof DocumentModel) {
            DocumentModel doc = (DocumentModel) environment.getSource();
            el.bindValue(elContext, "this", doc);
        }

        finalQuery = el.evaluateExpression(elContext, finalQuery, String.class);

        // session.query(request, response);
        // return session.query(finalQuery);

        return Collections.emptyList();
    }

    protected String getQuery(DataFetchingEnvironment environment) {
        return environment.getArgument("nxql");
    }
}
