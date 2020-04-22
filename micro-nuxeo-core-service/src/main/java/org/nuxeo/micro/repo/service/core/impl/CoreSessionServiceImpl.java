package org.nuxeo.micro.repo.service.core.impl;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService.CoreSessionRegistrationInfo;
import org.nuxeo.ecm.core.api.impl.NuxeoPrincipalImpl;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.micro.repo.RepoConfiguration;
import org.nuxeo.micro.repo.RepoConfigurationFactory;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.DefaultUIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.MockDocumentBloblManagerProvider;
import org.nuxeo.micro.repo.provider.impl.MongoDBRepositoryProviderImpl;
import org.nuxeo.micro.repo.service.core.CoreSessionService;
import org.nuxeo.micro.repo.service.schema.SchemaService;
import org.nuxeo.runtime.mongodb.MongoDBConnectionConfig;
import org.nuxeo.runtime.transaction.TransactionHelper;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;

public class CoreSessionServiceImpl implements CoreSessionService {

    private static final String TENANT_LOCAL_MAP_NAME = "__vertx.CoreSessionService.tenants";

    private final Vertx vertx;
    private JsonObject config;

    public CoreSessionServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;

    }

    @Override
    public void session(String tenantId, String username, Handler<AsyncResult<CoreSession>> sessionHandler) {

        //if (SchemaService.NUXEO_TENANTS_SCHEMA.equals(tenantId)) {
            lookupHolder(tenantId).sessionService(ssh -> {
                if (ssh.succeeded()) {
                    // TODO: Create a UserManagerService that resolve a principal base on username
                    // and tenant.
                    NuxeoPrincipalImpl p = new NuxeoPrincipalImpl(username, tenantId);
                    p.setAdministrator(username.endsWith("@nuxeo.com"));

                    TransactionHelper.runInTransaction(() -> {
                        CoreSession session = ssh.result().createCoreSession(p);
                        sessionHandler.handle(Future.succeededFuture(session));
                    });
                } else {
                    sessionHandler.handle(Future.failedFuture(ssh.cause()));
                }

            });
//        } else {
//            sessionHandler.handle(Future.failedFuture("not implemented"));
//        }

    }

    private void removeFromMap(LocalMap<String, CoreSessionServiceHolder> map, String dataSourceName) {
        synchronized (vertx) {
            map.remove(dataSourceName);
            if (map.isEmpty()) {
                map.close();
            }
        }
    }

    private CoreSessionServiceHolder lookupHolder(String tenantId) {

        synchronized (vertx) {

            LocalMap<String, CoreSessionServiceHolder> map = vertx.sharedData().getLocalMap(TENANT_LOCAL_MAP_NAME);

            CoreSessionServiceHolder theHolder = map.get(tenantId);
            if (theHolder == null) {
                MongoDBConnectionConfig mongoconfig = new MongoDBConnectionConfig();
                mongoconfig.server = config.getJsonObject("db").getString("server");
                mongoconfig.dbname = tenantId;
                mongoconfig.id = "default";

                theHolder = new CoreSessionServiceHolder(vertx, tenantId, mongoconfig,
                        () -> removeFromMap(map, tenantId));
                map.put(tenantId, theHolder);
            } else {
                theHolder.incRefCount();
            }
            return theHolder;
        }
    }

    private static class CoreSessionServiceHolder implements Shareable {
        Runnable closeRunner;
        int refCount = 1;
        private org.nuxeo.ecm.core.api.CoreSessionService css;
        private String tenantId;
        private MongoDBConnectionConfig mongoconfig;
        private Vertx vertx;

        public CoreSessionServiceHolder(Vertx vertx, String tenantId, MongoDBConnectionConfig mongoconfig,
                Runnable closeRunner) {
            this.vertx = vertx;
            this.closeRunner = closeRunner;
            this.tenantId = tenantId;
            this.mongoconfig = mongoconfig;
        }

        synchronized void sessionService(
                Handler<AsyncResult<org.nuxeo.ecm.core.api.CoreSessionService>> completionHandler) {
            if (css == null) {
                // Should be built by an Injection Manager (HK2, Guice....)

                SchemaService ss = SchemaService.createProxyWithCache(vertx);

                ss.getSchema(tenantId, sh -> {
                    if (sh.succeeded()) {

                        SchemaManagerProvider schemaManagerProvider = t -> sh.result();

                        DocumentBlobManagerProvider documentBlobManagerProvider = new MockDocumentBloblManagerProvider();
                        UIDGeneratorServiceProvider uidGenProvider = new DefaultUIDGeneratorServiceProvider();
                        RepoConfigurationFactory factory = new RepoConfigurationFactory(
                                new MongoDBRepositoryProviderImpl(schemaManagerProvider, documentBlobManagerProvider,
                                        uidGenProvider, mongoconfig));
                        RepoConfiguration configuration = TransactionHelper.runInTransaction(() -> {
                            return factory.getConfiguration(tenantId);

                        });

                        css = configuration.getCoreSessionService();
                        completionHandler.handle(Future.succeededFuture(css));

                    } else {
                        completionHandler.handle(Future.failedFuture(sh.cause()));
                    }

                });

            } else {
                completionHandler.handle(Future.succeededFuture(css));
            }
        }

        synchronized void incRefCount() {
            refCount++;
        }

        @SuppressWarnings("deprecation")
        synchronized void close() {
            if (--refCount == 0) {
                if (css != null) {
                    for (CoreSessionRegistrationInfo ri : css.getCoreSessionRegistrationInfos()) {
                        ri.getCoreSession().close();
                    }

                }
                if (closeRunner != null) {
                    closeRunner.run();
                }
            }
        }
    }
}
