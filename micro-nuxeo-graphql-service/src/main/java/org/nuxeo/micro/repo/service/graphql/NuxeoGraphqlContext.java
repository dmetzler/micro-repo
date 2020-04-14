package org.nuxeo.micro.repo.service.graphql;

import java.security.Principal;

import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.el.ExpressionEvaluator;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;

public interface NuxeoGraphqlContext {

    NuxeoCoreSessionVertxStub getSession();

    ExpressionEvaluator getEvaluator();

    SchemaManager getSchemaManager();

    Principal getPrincipal();
}
