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
package org.nuxeo.micro.repo.service.tenant.impl;

import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.utils.GrpcInterceptor;
import org.nuxeo.micro.repo.service.tenant.TenantConfiguration;
import org.nuxeo.micro.repo.service.tenant.TenantService;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxChannelBuilder;

public class TenantServiceImpl implements TenantService {

    private Vertx vertx;

    private NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub coreSession;

    public TenantServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;

        int corePort = 8787;
        String coreHost = "localhost";

        if (config.getJsonObject("core") != null) {
            corePort = config.getJsonObject("core").getInteger("port", 8787);
            coreHost = config.getJsonObject("core").getString("host", "localhost");
        }

        ManagedChannel channel = VertxChannelBuilder.forAddress(vertx, coreHost, corePort).usePlaintext(true).build();

        Metadata headers = new Metadata();
        headers.put(GrpcInterceptor.TENANTID_METADATA_KEY, TenantService.NUXEO_TENANTS_SCHEMA);

        coreSession = NuxeoCoreSessionGrpc.newVertxStub(channel)
                                          .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

    }

    public void getTenantConfiguration(String tenantId, Handler<AsyncResult<TenantConfiguration>> resultHandler) {
        vertx.executeBlocking(future -> {

            DocumentRequest req = DocumentRequest.newBuilder().setPath("/" + tenantId).build();

            coreSession.getDocument(req, dr -> {
                if (!dr.succeeded()) {
                    resultHandler.handle(Future.failedFuture("Tenant Not Found"));
                } else {
                    Document doc = dr.result();

                    resultHandler.handle(Future.succeededFuture(TenantConfiguration.from(doc)));
                }
            });

        }, resultHandler);
    }

}
