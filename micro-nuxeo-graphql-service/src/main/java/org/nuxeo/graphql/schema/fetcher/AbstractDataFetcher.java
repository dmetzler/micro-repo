package org.nuxeo.graphql.schema.fetcher;

import java.security.Principal;

import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.NuxeoGraphqlContext;

public abstract class AbstractDataFetcher  {



    protected NuxeoCoreSessionVertxStub getSession(Object ctx) {
        if (ctx instanceof NuxeoGraphqlContext) {
            return ((NuxeoGraphqlContext) ctx).getSession();
        }
        return null;
    }

    protected Principal getPrincipal(Object ctx) {
        if (ctx instanceof NuxeoGraphqlContext) {
            return ((NuxeoGraphqlContext) ctx).getPrincipal();
        }
        return null;
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
