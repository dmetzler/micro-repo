package org.nuxeo.micro.repo.service.graphql;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.schema.SchemaService;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.grpc.VertxChannelBuilder;

public class MetaGraphQLHandler implements Handler<RoutingContext> {

    public static final Metadata.Key<String> TENANT_ID_KEY = Metadata.Key.of("tenantId", ASCII_STRING_MARSHALLER);

    private static final Logger LOG = LoggerFactory.getLogger(MetaGraphQLHandler.class);

    protected final NuxeoCoreSessionVertxStub nuxeoSession;

    private Vertx vertx;

    private JsonObject config;

    private GraphQLService gqlService;

    private SchemaService schemaService;

    protected MetaGraphQLHandler(Vertx vertx, JsonObject config, NuxeoCoreSessionVertxStub nuxeoSession,
            GraphQLService gqlService, SchemaService schemaService) {
        this.vertx = vertx;
        this.nuxeoSession = nuxeoSession;
        this.config = config;
        this.gqlService = gqlService;
        this.schemaService = schemaService;

    }

    public static void create(Vertx vertx, JsonObject config,
            Handler<AsyncResult<MetaGraphQLHandler>> completionHandler) {

        int corePort = 8787;
        String coreHost = "localhost";

        if (config.getJsonObject("core") != null) {
            corePort = config.getJsonObject("core").getInteger("port", 8787);
            coreHost = config.getJsonObject("core").getString("host", "localhost");
        }

        ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, coreHost, corePort).usePlaintext(true).build();

        NuxeoCoreSessionVertxStub nuxeoSession = NuxeoCoreSessionGrpc.newVertxStub(channel);
        SchemaService schemaService = SchemaService.createProxy(vertx);

        GraphQLService.create(vertx, config, gqr -> {
            if (gqr.succeeded()) {
                MetaGraphQLHandler handler = new MetaGraphQLHandler(vertx, config, nuxeoSession, gqr.result(),
                        schemaService);
                completionHandler.handle(Future.succeededFuture(handler));
            } else {
                completionHandler.handle(Future.failedFuture(gqr.cause()));
            }
        });

    }

    @Override
    public void handle(RoutingContext event) {
        String tenantId = event.request().getParam("tenantId");

        gqlService.getGraphQL(tenantId, gqlR -> {
            if (gqlR.succeeded()) {
                schemaService.getSchema(tenantId, sr -> {
                    if (sr.succeeded()) {
                        GraphQLHandler gql = GraphQLHandler.create(gqlR.result()).queryContext(rc -> {

                            Metadata headers = new Metadata();
                            headers.put(TENANT_ID_KEY, tenantId);
                            return new NuxeoContext(rc,
                                    nuxeoSession.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers)),
                                    sr.result());
                        });
                        gql.handle(event);
                    } else {
                        LOG.warn(sr.cause(), sr.cause());
                        HttpServerResponse response = event.response();
                        response.setStatusCode(404);
                        response.end("SS Repository not found: " + sr.cause().getMessage());
                    }
                });
            } else {
                LOG.warn(gqlR.cause(), gqlR.cause());
                HttpServerResponse response = event.response();
                response.setStatusCode(404);
                response.end("GQL Repository not found: " + gqlR.cause().getMessage());
            }
        });

    }

    public Router router() {
        Router graphQLRouter = Router.router(vertx);

        graphQLRouter.route("/graphiql/*").handler(event -> {
            String tenantId = event.request().getParam("tenantId");

            gqlService.getGraphQL(tenantId, gqlR -> {

                if (gqlR.succeeded()) {
                    GraphiQLHandler graphiQLHandler = GraphiQLHandler.create(
                            new GraphiQLHandlerOptions().setEnabled(true)
                                                        .setGraphQLUri(String.format("/%s/graphql", tenantId)));
                    graphiQLHandler.graphiQLRequestHeaders(rc -> {
                        String token = rc.get("token");
                        return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    });
                    graphiQLHandler.handle(event);
                } else {
                    LOG.warn(gqlR.cause());
                    HttpServerResponse response = event.response();
                    response.setStatusCode(404);
                    response.end("Repository not found");
                }
            });
        });

        graphQLRouter.route("/graphql").handler(CorsHandlerImpl.create(config.getJsonObject("cors")));
        graphQLRouter.route("/graphql").handler(this);

        return graphQLRouter;

    }

}
