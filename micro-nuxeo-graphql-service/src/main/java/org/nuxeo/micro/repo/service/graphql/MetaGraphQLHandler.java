package org.nuxeo.micro.repo.service.graphql;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.nuxeo.ecm.core.api.impl.NuxeoPrincipalImpl;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.model.TenantsOperation;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.vertx.graphql.NuxeoGQLConfiguration;
import org.nuxeo.vertx.graphql.NuxeoGQLConfiguration.Builder;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.grpc.VertxChannelBuilder;

public class MetaGraphQLHandler implements Handler<RoutingContext> {

    public static final Metadata.Key<String> TENANT_ID_KEY = Metadata.Key.of("tenantId", ASCII_STRING_MARSHALLER);

    protected final NuxeoCoreSessionVertxStub nuxeoSession;

    private Vertx vertx;

    private JsonObject config;

    protected MetaGraphQLHandler(Vertx vertx, NuxeoCoreSessionVertxStub nuxeoSession, JsonObject config) {
        this.vertx = vertx;
        this.nuxeoSession = nuxeoSession;
        this.config = config;

    }

    public static MetaGraphQLHandler create(Vertx vertx, JsonObject config) {

        int corePort = 8787;
        String coreHost = "localhost";

        if (config.getJsonObject("core") != null) {
            corePort = config.getJsonObject("core").getInteger("port", 8787);
            coreHost = config.getJsonObject("core").getString("host", "localhost");
        }

        ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, coreHost, corePort).usePlaintext(true).build();

        return new MetaGraphQLHandler(vertx, NuxeoCoreSessionGrpc.newVertxStub(channel), config);
    }

    @Override
    public void handle(RoutingContext event) {
        String tenantId = event.request().getParam("tenantId");
        
        GraphQL graphQL = getGraphQL(tenantId);

        if (graphQL != null) {
            GraphQLHandler gql = GraphQLHandler.create(graphQL).queryContext(rc -> {

                Metadata headers = new Metadata();
                headers.put(TENANT_ID_KEY, tenantId);
                return new NuxeoContext(rc,
                        nuxeoSession.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers)));
            });
            gql.handle(event);
        } else {
            HttpServerResponse response = event.response();
            response.setStatusCode(404);
            response.end("Repository not found");
        }
    }

    private GraphQL getGraphQL(String tenantId) {

        if (SchemaService.NUXEO_TENANTS_SCHEMA.equals(tenantId)) {

            graphql.schema.idl.RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();

            Builder builder = NuxeoGQLConfiguration.builder()//
                                                   .runtimeWiring(runtimeWiring)
                                                   .configuration(TenantsOperation.class);

            TypeDefinitionRegistry typeDefinitionRegistry = builder.getTypeDefinitionRegistry();

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry,
                    runtimeWiring.build()); // (4)
            return GraphQL.newGraphQL(graphQLSchema)//
                          .build();
        } else {

            NuxeoGQLSchemaManager gqlManager = new NuxeoGQLSchemaManager(sm);

            GraphQLSchema graphQLSchema = gqlManager.getNuxeoSchema();
            return GraphQL.newGraphQL(graphQLSchema)//
                          .build();
        }

    }

    public Router router() {
        Router graphQLRouter = Router.router(vertx);

        graphQLRouter.route("/graphiql/*").handler(event -> {
            String tenantId = event.request().getParam("tenantId");

            GraphQL graphQL = getGraphQL(tenantId);

            if (graphQL != null) {
                GraphiQLHandler graphiQLHandler = GraphiQLHandler.create(
                        new GraphiQLHandlerOptions().setEnabled(true)
                                                    .setGraphQLUri(String.format("/%s/graphql", tenantId)));
                graphiQLHandler.graphiQLRequestHeaders(rc -> {
                    String token = rc.get("token");
                    return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                });
                graphiQLHandler.handle(event);
            } else {
                HttpServerResponse response = event.response();
                response.setStatusCode(404);
                response.end("Repository not found");
            }
        });

        graphQLRouter.route("/graphql").handler(CorsHandlerImpl.create(config.getJsonObject("cors")));
        graphQLRouter.route("/graphql").handler(this);

        return graphQLRouter;

    }

}
