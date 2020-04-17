package org.nuxeo.micro.repo.service.graphql;

import org.nuxeo.micro.repo.service.BaseVerticle;
import org.nuxeo.micro.repo.service.schema.impl.SchemaVerticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.auth.ChainAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class GraphQLVerticle extends BaseVerticle {

    public static void main(String[] args) {
        ClusterManager mgr = new InfinispanClusterManager();

        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                vertx.deployVerticle(new GraphQLVerticle());
            } else {
                // failed!
            }
        });
    }

    @Override
    public void startWithConfig(JsonObject config, Handler<AsyncResult<Void>> completionHandler) {
        long start = System.currentTimeMillis();
        Router router = Router.router(vertx);
        // WebClient client = WebClient.create(vertx);
        // client.getAbs(config.getString("jwtIssuer") + "/.well-known/jwks.json").ssl(true).send(result -> {
        // if (result.succeeded()) {
        // // Obtain response
        // HttpResponse<Buffer> response = result.result();
        // JsonArray jwksKeys = response.bodyAsJsonObject().getJsonArray("keys");

        // setupAuthentication(config, router, jwksKeys);
        Integer port = config.getInteger("port", 8080);


        router.route().handler(BodyHandler.create());

        MetaGraphQLHandler.create(vertx, config, ar -> {
            if (ar.succeeded()) {

                router.mountSubRouter("/:tenantId", ar.result().router());

                vertx.createHttpServer()//
                     .requestHandler(router)//
                     .listen(port, http -> {
                         if (http.succeeded()) {
                             log.info(String.format("HTTP server started on port %d", port));
                             log.info(String.format("Started [%s] in %dms", this.getClass().getCanonicalName(),
                                     System.currentTimeMillis() - start));
                             completionHandler.handle(Future.succeededFuture());
                         } else {
                             completionHandler.handle(Future.failedFuture(http.cause()));
                         }
                     });
            } else {
                completionHandler.handle(Future.failedFuture(ar.cause()));
                vertx.createHttpServer()

                     .requestHandler(req -> req.response()
                                               .setStatusCode(500)
                                               .setStatusMessage(ar.cause().getMessage())
                                               .end("KO"))
                     .listen(port);

            }
        });

    }

    private void setupAuthentication(JsonObject config, Router router, JsonArray jwksKeys) {
        ChainAuth chainAuth = ChainAuth.create();
        ChainAuthHandler authChainHandler = ChainAuthHandler.create();

        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)).setAuthProvider(chainAuth));

        // JWT auth
        JsonObject jwksConfig = new JsonObject().put("jwks", jwksKeys);
        JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions(jwksConfig));
        chainAuth.append(provider);
        authChainHandler.append(JWTAuthHandler.create(provider));

        // OAuth auth
        if (config.containsKey("oauth")) {
            OAuth2Auth oauth = getOAuth(config.getJsonObject("oauth"));
            chainAuth.append(oauth);
            authChainHandler.append(
                    OAuth2AuthHandler.create(oauth, config.getJsonObject("oauth").getString("redirect_uri")) //
                                     .setupCallback(router.route("/callback"))//
                                     .addAuthority("openid profile email"));
        }

        router.route().handler(authChainHandler);
    }

    protected OAuth2Auth getOAuth(JsonObject config) {

        String clientId = config.getString("clientId");
        String clientSecret = config.getString("clientSecret");
        String basePath = config.getString("basePath");
        String tokenPath = config.getString("tokenPath");
        String authorizationPath = config.getString("authorizationPath");
        String userInfoPath = config.getString("userInfoPath");
        String scopeSeparator = config.getString("scopeSeparator", " ");

        return OAuth2Auth.create(vertx, new OAuth2ClientOptions()//
                                                                 .setFlow(OAuth2FlowType.AUTH_CODE) //
                                                                 .setSite(basePath)//
                                                                 .setTokenPath(tokenPath)//
                                                                 .setAuthorizationPath(authorizationPath)//
                                                                 .setUserInfoPath(userInfoPath)//
                                                                 .setScopeSeparator(scopeSeparator)//
                                                                 .setClientID(clientId)//
                                                                 .setClientSecret(clientSecret)//
                                                                 .setHeaders(new JsonObject()//
                                                                                             .put("User-Agent",
                                                                                                     "vertx-auth-oauth2")));

    }

}
