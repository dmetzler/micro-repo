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
import org.nuxeo.micro.repo.proto.QueryRequest;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;

public class NxqlQueryDataFetcher extends AbstractDataFetcher<List<Document>> {

    @Override
    public void get(DataFetchingEnvironment environment, Promise<List<Document>> future) {

        NuxeoCoreSessionVertxStub session = getSession(environment.getContext());
        if (session == null) {
            future.complete(Collections.emptyList());
        }

        getPrincipal(environment.getContext(), pr -> {
            if (pr.succeeded()) {
                ExpressionEvaluator el = getEl(environment.getContext());
                String finalQuery = getQuery(environment);
                ELService elService = new ELServiceServiceImpl();
                ELContext elContext = elService.createELContext();
                el.bindValue(elContext, "principal", pr.result());

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

                QueryRequest qreq = QueryRequest.newBuilder().setNxql(finalQuery).build();
                session.query(qreq, qrr -> {
                    if (qrr.succeeded()) {
                        future.complete(qrr.result().getDocsList());
                    } else {
                        future.fail(qrr.cause());
                    }
                });

            } else {
                future.fail(pr.cause());
            }
        });

    }

    protected String getQuery(DataFetchingEnvironment environment) {
        return environment.getArgument("nxql");
    }

}
