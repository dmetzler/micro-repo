/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     dmetzler
 */
package org.nuxeo.micro.repo.service.graphql.impl;

import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.graphql.descriptors.GraphQLFeature;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.repo.service.dsl.NuxeoDslService;
import org.nuxeo.micro.repo.service.graphql.GraphQLService;
import org.nuxeo.micro.repo.service.graphql.model.TenantsOperation;
import org.nuxeo.micro.repo.service.graphql.tenant.NuxeoGQLConfiguration;
import org.nuxeo.micro.repo.service.graphql.tenant.NuxeoGQLConfiguration.Builder;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.micro.repo.service.tenant.TenantConfiguration;
import org.nuxeo.micro.repo.service.tenant.TenantService;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class GraphQLServiceImpl implements GraphQLService {

    private Vertx vertx;

    private JsonObject config;

    private SchemaService schemaService;

    private TenantService tenantService;

    private NuxeoDslService dslService;

    public GraphQLServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        schemaService = SchemaService.createProxy(vertx);
        tenantService = TenantService.createProxy(vertx);
        dslService = NuxeoDslService.createProxy(vertx);

    }

    @Override
    public void getGraphQL(String tenantId, Handler<AsyncResult<GraphQL>> completionHandler) {
        if (TenantService.NUXEO_TENANTS_SCHEMA.equals(tenantId)) {

            graphql.schema.idl.RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();

            Builder builder = NuxeoGQLConfiguration.builder()//
                                                   .runtimeWiring(runtimeWiring)
                                                   .configuration(TenantsOperation.class);

            TypeDefinitionRegistry typeDefinitionRegistry = builder.getTypeDefinitionRegistry();

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry,
                    runtimeWiring.build());
            completionHandler.handle(Future.succeededFuture(GraphQL.newGraphQL(graphQLSchema).build()));
        } else {
            tenantService.getTenantConfiguration(tenantId, cr -> {

                if (cr.succeeded()) {
                    TenantConfiguration tenantConfiguration = cr.result();
                    dslService.getAbstracSyntaxTree(tenantConfiguration.getDsl(), dslr -> {
                        if (dslr.succeeded()) {

                            DslModel model = DslModel.builder().with(GraphQLFeature.class).build();
                            model.visit(dslr.result());
                            GraphQLFeature feature = model.getFeature(GraphQLFeature.class);

                            schemaService.getSchema(tenantId, sr -> {
                                if (sr.succeeded()) {
                                    SchemaManager sm = sr.result();
                                    NuxeoGQLSchemaManager gqlManager = new NuxeoGQLSchemaManager(feature.getAliases(),
                                            feature.getQueries(), feature.getCruds(), sm);
                                    GraphQLSchema graphQLSchema = gqlManager.getNuxeoSchema();

                                    completionHandler.handle(
                                            Future.succeededFuture(GraphQL.newGraphQL(graphQLSchema).build()));
                                } else {
                                    completionHandler.handle(Future.failedFuture(sr.cause()));
                                }

                            });
                        } else {
                            completionHandler.handle(Future.failedFuture(dslr.cause()));
                        }

                    });
                } else {
                    completionHandler.handle(Future.failedFuture(cr.cause()));
                }
            });

        }

    }

}
