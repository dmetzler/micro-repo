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
package org.nuxeo.micro.repo.service.graphql;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.CorsHandler;

public class GraphQLCorsHandler {

    private GraphQLCorsHandler() {

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
            config.getJsonArray("allowedMethods")
                  .stream()
                  .forEach(o -> allowedMethods.add(HttpMethod.valueOf((String) o)));
        }

        return CorsHandler.create("http.*")//
                          .allowedHeaders(allowedHeaders)//
                          .allowedMethods(allowedMethods)//
                          .allowCredentials(true);

    }
}
