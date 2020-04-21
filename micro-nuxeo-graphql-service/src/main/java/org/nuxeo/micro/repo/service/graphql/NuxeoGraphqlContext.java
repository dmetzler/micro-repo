package org.nuxeo.micro.repo.service.graphql;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface NuxeoGraphqlContext {

    ExpressionEvaluator getEvaluator();

    SchemaManager getSchemaManager();

    void getPrincipal(Handler<AsyncResult<NuxeoPrincipal>> completionHandler);
}
