/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Florent Guillaume
 */
package org.nuxeo.micro;

import org.nuxeo.ecm.core.blob.DocumentBlobManager;
import org.nuxeo.ecm.core.repository.RepositoryFactory;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.storage.dbs.DBSRepositoryFactory;

import com.mongodb.client.MongoDatabase;

/**
 * MongoDB implementation of a {@link RepositoryFactory}, creating a {@link MongoDBRepository}.
 *
 * @since 5.9.4
 */
public class MongoDBRepositoryFactory extends DBSRepositoryFactory {

    private MongoDatabase db;

    private SchemaManager sm;

    private DocumentBlobManager dbm;

    public MongoDBRepositoryFactory(String repositoryName, MongoDatabase db, SchemaManager sm,
            DocumentBlobManager dbm) {
        super(repositoryName);
        this.db = db;
        this.sm = sm;
        this.dbm = dbm;
    }

    @Override
    public Object call() {
        return new MongoDBRepository(db, installPool(), (MongoDBRepositoryDescriptor) getRepositoryDescriptor(), null,
                null, sm, dbm);

    }

}
