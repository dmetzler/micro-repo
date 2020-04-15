package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.NuxeoGraphqlContext;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public abstract class AbstractDataFetcher  {



    protected NuxeoCoreSessionVertxStub getSession(Object ctx) {
        if (ctx instanceof NuxeoGraphqlContext) {
            return ((NuxeoGraphqlContext) ctx).getSession();
        }
        return null;
    }

    protected void getPrincipal(Object ctx, Handler<AsyncResult<NuxeoPrincipal>> completionHandler) {
        if (ctx instanceof NuxeoGraphqlContext) {
            ((NuxeoGraphqlContext) ctx).getPrincipal(completionHandler);
        }
    }

    protected ExpressionEvaluator getEl(Object ctx) {
        if (ctx instanceof NuxeoGraphqlContext) {
            return ((NuxeoGraphqlContext) ctx).getEvaluator();
        }
        return null;
    }

    protected SchemaManager getSchemaManager(Object ctx) {
        if (ctx instanceof NuxeoGraphqlContext) {
            return ((NuxeoGraphqlContext) ctx).getSchemaManager();
        }
        return null;
    }
}
