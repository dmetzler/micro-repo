package org.nuxeo.micro.repo.service.graphql;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.CorsHandler;

public class CorsHandlerImpl {

    private CorsHandlerImpl() {

    }

    public static CorsHandler create(JsonObject config) {
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Access-Control-Allow-Headers");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("authorization");
        allowedHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.POST);

        if (config != null && config.containsKey("allowedHeaders")) {
            config.getJsonArray("allowedHeaders").stream().forEach(o -> allowedHeaders.add((String) o));
        }
        if (config != null && config.containsKey("allowedMethods")) {
            config.getJsonArray("allowedMethods").stream()
                    .forEach(o -> allowedMethods.add(HttpMethod.valueOf((String) o)));
        }

        return CorsHandler.create("http.*")//
                .allowedHeaders(allowedHeaders)//
                .allowedMethods(allowedMethods)//
                .allowCredentials(true);

    }
}
