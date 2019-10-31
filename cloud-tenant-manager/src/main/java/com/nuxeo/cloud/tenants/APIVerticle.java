package com.nuxeo.cloud.tenants;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.jtajca.JtaActivator;

import com.nuxeo.cloud.tenants.model.Tenant;
import com.nuxeo.cloud.tenants.nuxeo.CoreSessionClient;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

public class APIVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(APIVerticle.class);

    private JtaActivator jta;

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new APIVerticle());
    }

    @Override
    public void start(Promise<Void> fut) throws Exception {
        log.info(String.format("Starting [%s]", APIVerticle.class.getCanonicalName()));
        long start = System.currentTimeMillis();

        // Activate JTA
        jta = new JtaActivator();
        jta.activate();


        ConfigRetriever retriever = ConfigRetriever.create(vertx, //
                new ConfigRetrieverOptions().addStore(//
                        new ConfigStoreOptions().setType("file")//
                                .setFormat("yaml")//
                                .setOptional(true)//
                                .setConfig(new JsonObject()//
                                        .put("path", "config/application.yaml"))));


        retriever.getConfig(config -> {
            if (config.failed()) {
                fut.fail(config.cause());
            } else {

                CoreSessionClient.create(vertx, "tenants", cs -> {

                    if (cs.succeeded()) {

                        Router router = Router.router(vertx);

                        // AuthHandler authHandler = getOAuthHandler(router);

                        AuthHandler authHandler = BasicAuthHandler.create(new BasicAuthProvider());

                        GraphQL graphQL = setupGraphQLJava();
                        GraphiQLHandlerOptions options = new GraphiQLHandlerOptions().setEnabled(true);

                        GraphiQLHandler graphiQLHandler = GraphiQLHandler.create(options);
                        graphiQLHandler.graphiQLRequestHeaders(rc -> {
                            String token = rc.get("token");
                            return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                        });

                        router.route("/").handler(authHandler);
                        router.route("/graphiql/*").handler(graphiQLHandler);
                        router.route("/graphql")//
                                .handler(GraphQLHandler.create(graphQL)
                                        .queryContext(rc -> new NuxeoContext(rc, cs.result())));
                        Integer port = config.result().getInteger("port", 8080);
                        vertx.createHttpServer()//
                                .requestHandler(router)//
                                .listen(port, http -> {
                                    if (http.succeeded()) {
                                        fut.complete();
                                        log.info(String.format("HTTP server started on port %d", port));
                                        log.info(String.format("Started [%s] in %dms",
                                                APIVerticle.class.getCanonicalName(),
                                                System.currentTimeMillis() - start));
                                    } else {
                                        fut.fail(http.cause());
                                    }
                                });
                    } else {
                        fut.fail(cs.cause());
                    }

                });

            }
        });
    }

    public void stop(Promise<Void> fut) {

        jta.deactivate();
        fut.complete();
    }

    private GraphQL setupGraphQLJava() {
        String schema = vertx.fileSystem().readFileBlocking("tenants.graphqls").toString(); // (1)

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema); // (2)

        VertxDataFetcher<List<Tenant>> allTenantByCustomerId = new VertxDataFetcher<>((environment, future) -> {
            allTenantsByCustomerId(environment, future);
        });

        VertxDataFetcher<Tenant> tenantById = new VertxDataFetcher<>((environment, future) -> {
            tenantById(environment, future);
        });

        VertxDataFetcher<Tenant> newTenant = new VertxDataFetcher<>((environment, future) -> {
            newTenant(environment, future);
        });

        RuntimeWiring runtimeWiring = newRuntimeWiring() // (3)

                .type("Query", builder -> builder.dataFetcher("allTenantsByCustomer", allTenantByCustomerId))
                .type("Query", builder -> builder.dataFetcher("tenantById", tenantById))
                .type("Mutation", builder -> builder.dataFetcher("newTenant", newTenant)).build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring); // (4)

        return GraphQL.newGraphQL(graphQLSchema).build(); // (5)
    }

    private void allTenantsByCustomerId(DataFetchingEnvironment env, Promise<List<Tenant>> fut) {
        NuxeoContext nc = env.getContext();

        String customerId = env.getArgument("customerId");

        String query = String.format("SELECT * FROM Tenant WHERE tn:customerId = '%s'", customerId);
        nc.query(query, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                fut.complete(ar.result().stream().map(Tenant::fromDoc).collect(toList()));
            } else {
                fut.fail(ar.cause());
            }
        });

    }

    private void tenantById(DataFetchingEnvironment env, Promise<Tenant> fut) {
        NuxeoContext nc = env.getContext();
        String tenantId = env.getArgument("tenantId");

        String query = String.format("SELECT * FROM Tenant WHERE ecm:uuid = '%s'", tenantId);
        nc.query(query, new JsonObject(), ar -> {

            if (ar.succeeded()) {
                Optional<Tenant> tenant = ar.result().stream().map(Tenant::fromDoc).findFirst();
                if (tenant.isPresent()) {
                    fut.complete(tenant.get());
                } else {
                    fut.fail("No tenant whith id " + tenantId);
                }
            } else {
                fut.fail(ar.cause());
            }
        });


    }

    private void newTenant(DataFetchingEnvironment env, Promise<Tenant> fut) {
        NuxeoContext nuxeo = env.getContext();
        String tenantId = env.getArgument("tenantId");
        String customerId = env.getArgument("customerId");

        nuxeo.session(cs -> {
            if (cs.succeeded()) {
                try (CloseableCoreSession session = cs.result()) {
                    PathRef docRef = new PathRef("/" + customerId);
                    if (!session.exists(docRef)) {
                        DocumentModel customerDoc = session.createDocumentModel("/", customerId, "Workspace");
                        customerDoc = session.createDocument(customerDoc);
                    }

                    Tenant tenant = new Tenant(customerId, tenantId);
                    DocumentModel tenantDoc =  session.createDocument(tenant.toDoc(session));
                    fut.complete(Tenant.fromDoc(tenantDoc));
                }
            } else {
                fut.fail(cs.cause());
            }

        });
    }

}
