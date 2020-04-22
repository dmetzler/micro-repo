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
package org.nuxeo.micro.repo.service.schema;

import org.nuxeo.micro.repo.service.tenant.TenantCache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class SchemaServiceWithCache extends TenantCache<RemoteSchemaManager> implements SchemaService {

    private SchemaService delegate;

    /**
     *
     */
    public SchemaServiceWithCache(Vertx vertx, SchemaService delegate) {
        super(vertx);
        this.delegate = delegate;
    }

    @Override
    public void getSchema(String tenantId, Handler<AsyncResult<RemoteSchemaManager>> completionHandler) {
        get(tenantId, completionHandler, delegate::getSchema);
    }

}
