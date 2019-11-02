package org.nuxeo.micro.repo.provider.impl;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.repository.RepositoryFactory;
import org.nuxeo.micro.MongoDBRepositoryFactory;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;
import org.nuxeo.micro.repo.provider.RepositoryProvider;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;
import org.nuxeo.runtime.mongodb.MongoDBComponent;
import org.nuxeo.runtime.mongodb.MongoDBConnectionConfig;

import com.mongodb.client.MongoDatabase;

public class MongoDBRepositoryProviderImpl implements RepositoryProvider{


    private DocumentBlobManagerProvider dbmProvider;
    private SchemaManagerProvider schemaProvider;
    private UIDGeneratorServiceProvider uidGeneratorServiceProvider;
    private MongoDBConnectionConfig mongoconfig;

    public MongoDBRepositoryProviderImpl(SchemaManagerProvider schemaProvider, DocumentBlobManagerProvider dbmProvider, UIDGeneratorServiceProvider uidGeneratorServiceProvider, MongoDBConnectionConfig mongoconfig) {
        this.schemaProvider = schemaProvider;
        this.dbmProvider = dbmProvider;
        this.uidGeneratorServiceProvider = uidGeneratorServiceProvider ;
        this.mongoconfig = mongoconfig;
    }


    @Override
    public Repository getForTenant(String tenantId) {
        MongoDatabase db = getMongoDB();

        RepositoryFactory repositoryFactory = new MongoDBRepositoryFactory(tenantId, db, schemaProvider.getForTenant(tenantId),
                dbmProvider.getForTenant(tenantId), uidGeneratorServiceProvider.getForTenant(tenantId));
        return (Repository) repositoryFactory.call();
    }


    private MongoDatabase getMongoDB() {
        MongoDBComponent mongoComponent = new MongoDBComponent();
        // Init component to hold MongoDB config
        mongoComponent.addDescriptor(mongoconfig);
        mongoComponent.init();
        MongoDatabase db = mongoComponent.getDatabase("default");
        return db;
    }


}
