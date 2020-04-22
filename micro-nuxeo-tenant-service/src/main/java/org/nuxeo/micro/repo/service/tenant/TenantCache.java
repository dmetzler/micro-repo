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

import java.util.function.BiConsumer;

import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

public class TenantCache<T> {

    protected Cache<String, T> cache = CacheBuilder.newBuilder().maximumSize(100).build();

    private MessageConsumer<String> invalidationConsumer;

    /**
     *
     */
    public TenantCache(Vertx vertx) {
        invalidationConsumer = vertx.eventBus().consumer(TenantService.INVALIDATION_ADDRESS);
        invalidationConsumer.handler(this::handleInvalidationEvent);
    }

    private void handleInvalidationEvent(Message<String> tenantId) {
        cache.invalidate(tenantId.body());
    }

    protected void get(String tenantId, Handler<AsyncResult<T>> completionHandler,
            BiConsumer<String, Handler<AsyncResult<T>>> delegate) {

        StopWatch watch = new StopWatch();
        watch.start();
        @Nullable
        T item = cache.getIfPresent(tenantId);
        if (item != null) {
            completionHandler.handle(Future.succeededFuture(item));
        } else {
            delegate.accept(tenantId, tr -> {
                if (tr.succeeded()) {
                    completionHandler.handle(Future.succeededFuture(tr.result()));
                    cache.put(tenantId, tr.result());
                } else {
                    completionHandler.handle(Future.failedFuture(tr.cause()));
                }
            });
        }
    }

    public static void invalidateTenant(String tenantId, Vertx vertx) {
        vertx.eventBus().publish(TenantService.INVALIDATION_ADDRESS, tenantId);
    }

}
