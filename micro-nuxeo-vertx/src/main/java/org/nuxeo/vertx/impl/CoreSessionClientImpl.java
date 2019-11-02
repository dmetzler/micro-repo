package org.nuxeo.vertx.impl;

import java.util.List;
import java.util.Objects;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.CoreSessionService.CoreSessionRegistrationInfo;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.micro.repo.RepoConfiguration;
import org.nuxeo.micro.repo.RepoConfigurationFactory;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;
import org.nuxeo.micro.repo.provider.TenantSchemaUrlResolver;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.ClassPathSchemaUrlResolver;
import org.nuxeo.micro.repo.provider.impl.DSLSchemaManagerProvider;
import org.nuxeo.micro.repo.provider.impl.DefaultUIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.MockDocumentBloblManagerProvider;
import org.nuxeo.micro.repo.provider.impl.MongoDBRepositoryProviderImpl;
import org.nuxeo.runtime.mongodb.MongoDBConnectionConfig;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.nuxeo.vertx.CoreSessionClient;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;

public class CoreSessionClientImpl implements CoreSessionClient, Closeable {

    Vertx vertx;
    JsonObject config;

    CoreSessionHolder holder;

    CoreSessionService css;
    String tenantId;
    private MongoDBConnectionConfig mongoconfig;

    private static final String DS_LOCAL_MAP_NAME = "__vertx.CoreSessionClient.datasources";

    public CoreSessionClientImpl(Vertx vertx, String tenantId, JsonObject config, ClassLoader schemaClassLoader) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(config);
        Objects.requireNonNull(tenantId);
        this.vertx = vertx;
        this.config = config;
        this.tenantId = tenantId;

        Context context = vertx.getOrCreateContext();
        context.addCloseHook(this);

        this.holder = lookupHolder(tenantId, config, schemaClassLoader);
        this.css = holder.sessionService();
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        holder.close();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public CoreSessionClient query(String query, NuxeoPrincipal principal, JsonObject options,
            Handler<AsyncResult<List<DocumentModel>>> resultHandler) {

        session(principal, sessionHandler -> {
            try (CloseableCoreSession session = sessionHandler.result()) {
                DocumentModelList docs = session.query(query);
                resultHandler.handle(Future.succeededFuture(docs));

            } catch (Exception e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });

        return this;
    }

    @Override
    public CoreSessionClient getDocument(DocumentRef docRef, NuxeoPrincipal principal, JsonObject jsonObject,
            Handler<AsyncResult<@Nullable DocumentModel>> resultHandler) {

        session(principal, sessionHandler -> {
            try (CloseableCoreSession session = sessionHandler.result()) {
                if (session.exists(docRef)) {
                    resultHandler.handle(Future.succeededFuture(session.getDocument(docRef)));
                } else {
                    resultHandler.handle(
                            Future.failedFuture(new DocumentNotFoundException(String.format("Ref(%s)", docRef))));
                }

            }
        });

        return this;
    }

    @Override
    public CoreSessionClient session(NuxeoPrincipal principal,
            Handler<AsyncResult<CloseableCoreSession>> resultHandler) {

        TransactionHelper.runInTransaction(() -> {
            CloseableCoreSession coreSession = css.createCoreSession(principal);
            resultHandler.handle(Future.succeededFuture(coreSession));
        });
        return this;
    }

    private void removeFromMap(LocalMap<String, CoreSessionHolder> map, String dataSourceName) {
        synchronized (vertx) {
            map.remove(dataSourceName);
            if (map.isEmpty()) {
                map.close();
            }
        }
    }

    private CoreSessionHolder lookupHolder(String datasourceName, JsonObject config, ClassLoader schemaClassLoader) {

        synchronized (vertx) {

            mongoconfig = new MongoDBConnectionConfig();
            mongoconfig.server = config.getJsonObject("db").getString("server");
            mongoconfig.dbname = config.getJsonObject("db").getString("dbname");
            mongoconfig.id = "default";

            LocalMap<String, CoreSessionHolder> map = vertx.sharedData().getLocalMap(DS_LOCAL_MAP_NAME);

            CoreSessionHolder theHolder = map.get(datasourceName);
            if (theHolder == null) {
                theHolder = new CoreSessionHolder(tenantId, mongoconfig, () -> removeFromMap(map, datasourceName), schemaClassLoader);
                map.put(datasourceName, theHolder);
            } else {
                theHolder.incRefCount();
            }
            return theHolder;
        }
    }

    private static class CoreSessionHolder implements Shareable {
        Runnable closeRunner;
        int refCount = 1;
        private CoreSessionService css;
        private String tenantId;
        private MongoDBConnectionConfig mongoconfig;
        private ClassLoader schemaClassLoader;


        public CoreSessionHolder(String tenantId, MongoDBConnectionConfig mongoconfig, Runnable closeRunner, ClassLoader schemaClassLoader) {
            this.closeRunner = closeRunner;
            this.tenantId = tenantId;
            this.mongoconfig = mongoconfig;
            this.schemaClassLoader = schemaClassLoader;
        }

        synchronized CoreSessionService sessionService() {
            if (css == null) {
                // Should be built by an Injection Manager (HK2, Guice....)

                TenantSchemaUrlResolver urlProvider = new ClassPathSchemaUrlResolver(schemaClassLoader);
                SchemaManagerProvider schemaManagerProvider = new DSLSchemaManagerProvider(urlProvider);
                DocumentBlobManagerProvider documentBlobManagerProvider = new MockDocumentBloblManagerProvider();
                UIDGeneratorServiceProvider uidGenProvider = new DefaultUIDGeneratorServiceProvider();
                RepoConfigurationFactory factory = new RepoConfigurationFactory(new MongoDBRepositoryProviderImpl(
                        schemaManagerProvider, documentBlobManagerProvider, uidGenProvider, mongoconfig ));
                RepoConfiguration configuration = TransactionHelper.runInTransaction(() -> {
                    return factory.getConfiguration(tenantId);

                });

                css = configuration.getCoreSessionService();
            }
            return css;
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

    @Override
    public void close() {
        holder.close();
    }
}
