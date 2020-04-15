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
package org.nuxeo.micro.repo.service.tenant;

import org.nuxeo.micro.repo.proto.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class TenantConfiguration {

    @JsonProperty("dsl")
    private String dsl;

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    public TenantConfiguration(JsonObject json) {
        this.dsl = json.getString("dsl");
        this.name = json.getString("name");
        this.id = json.getString("id");
    }

    private TenantConfiguration() {
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public String getDsl() {
        return dsl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public static class Builder {

        private String dsl;

        private String id;

        private String name;

        public Builder dsl(String dsl) {
            this.dsl = dsl;
            return this;
        }

        public TenantConfiguration build() {
            TenantConfiguration conf = new TenantConfiguration();
            conf.dsl = this.dsl;
            conf.name = this.name;
            conf.id = this.id;
            return conf;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

    }

    public static TenantConfiguration from(Document doc) {
        String dsl = doc.getPropertiesMap().get("tenant:schemaDef").getScalarValue(0).getStrValue();
        return new Builder().name(doc.getName()).id(doc.getUuid()).dsl(dsl).build();

    }
}
