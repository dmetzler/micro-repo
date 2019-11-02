package org.nuxeo.vertx;

import org.nuxeo.runtime.jtajca.JtaActivator;

import graphql.GraphQL;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;

public abstract class Nuxicle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(Nuxicle.class);

    private JtaActivator jta;

    @Override
    public void start(Promise<Void> fut) throws Exception {
        log.info(String.format("Starting [%s]", Nuxicle.class.getCanonicalName()));
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

        retriever.getConfig(ar -> {
            if (ar.failed()) {
                fut.fail(ar.cause());
            } else {

                JsonObject config = ar.result();

                CoreSessionClient.create(vertx, config.getJsonObject("nuxeo").getString("tenantId"), config,
                        this.getClass().getClassLoader(), cs -> {

                            if (cs.succeeded()) {

                                Router router = Router.router(vertx);

                                OAuth2Auth oauth = getOAuth(config.getJsonObject("oauth"));
                                router.route().handler(
                                        SessionHandler.create(LocalSessionStore.create(vertx)).setAuthProvider(oauth));

                                AuthHandler authHandler = OAuth2AuthHandler.create(oauth) //
                                        .setupCallback(router.route("/callback"))//
                                        .addAuthority("user:email");
                                router.route("/*").handler(authHandler);

                                GraphiQLHandler graphiQLHandler = GraphiQLHandler
                                        .create(new GraphiQLHandlerOptions().setEnabled(true));
                                graphiQLHandler.graphiQLRequestHeaders(rc -> {
                                    String token = rc.get("token");
                                    return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION,
                                            "Bearer " + token);
                                });
                                router.route("/graphiql/*").handler(graphiQLHandler);

                                // Setup the GraphQL Schema

                                GraphQL graphQL = configureGraphQL();

                                router.route("/graphql")//
                                        .handler(GraphQLHandler.create(graphQL)
                                                .queryContext(rc -> new NuxeoContext(rc, cs.result())));

                                Integer port = config.getInteger("port", 8080);
                                vertx.createHttpServer()//
                                        .requestHandler(router)//
                                        .listen(port, http -> {
                                            if (http.succeeded()) {
                                                fut.complete();
                                                log.info(String.format("HTTP server started on port %d", port));
                                                log.info(String.format("Started [%s] in %dms",
                                                        this.getClass().getCanonicalName(),
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

    protected abstract GraphQL configureGraphQL();

    protected OAuth2Auth getOAuth(JsonObject config) {

        String clientId = config.getString("clientId");
        String clientSecret = config.getString("clientSecret");
        String basePath = config.getString("basePath");
        String tokenPath = config.getString("tokenPath");
        String authorizationPath = config.getString("authorizationPath");
        String userInfoPath = config.getString("userInfoPath");
        String scopeSeparator = config.getString("scopeSeparator", " ");

        return OAuth2Auth.create(vertx, new OAuth2ClientOptions().setFlow(OAuth2FlowType.AUTH_CODE) //
                .setSite(basePath)//
                .setTokenPath(tokenPath)//
                .setAuthorizationPath(authorizationPath)//
                .setUserInfoPath(userInfoPath)//
                .setScopeSeparator(scopeSeparator)//
                .setClientID(clientId)//
                .setClientSecret(clientSecret)//
                .setHeaders(new JsonObject()//
                        .put("User-Agent", "vertx-auth-oauth2")));

    }

    @Override
    public void stop(Promise<Void> fut) {

        jta.deactivate();
        fut.complete();
    }
}
