/*
 * (C) Copyright 2017-2018 Nuxeo (http://nuxeo.com/) and others.
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
 *     Kevin Leturc
 */
package org.nuxeo.runtime.mongodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Component used to get a database connection to MongoDB. Don't expose {@link MongoClient} directly, because it's this
 * component which is responsible for creating and closing it.
 *
 * @since 9.1
 */
public class MongoDBComponent implements MongoDBConnectionService {

    private static final Logger log = LoggerFactory.getLogger(MongoDBComponent.class);

    private static final String DEFAULT_CONNECTION_ID = "default";

    private Map<String, MongoDBConnectionConfig> descriptors = new HashMap<>();

    private final Map<String, MongoClient> clients = new ConcurrentHashMap<>();

    public void addDescriptor(MongoDBConnectionConfig config) {
        descriptors.put(config.id, config);
    }

    /**
     * @param id the connection id to retrieve.
     * @return the database configured by {@link MongoDBConnectionConfig} for the input id, or the default one if it
     *         doesn't exist
     */
    @SuppressWarnings("resource") // client closed by stop()
    @Override
    public MongoDatabase getDatabase(String id) {
        MongoDBConnectionConfig config = descriptors.get(id);
        MongoClient client = clients.get(id);
        if (client == null) {
            config = descriptors.get(DEFAULT_CONNECTION_ID);
            client = clients.get(DEFAULT_CONNECTION_ID);
        }
        return MongoDBConnectionHelper.getDatabase(client, config.dbname);
    }

    /**
     * @return all configured databases
     */
    @Override
    public Iterable<MongoDatabase> getDatabases() {
        return () -> clients.entrySet().stream().map(e -> {
            MongoDBConnectionConfig c = descriptors.get(e.getKey());
            return MongoDBConnectionHelper.getDatabase(e.getValue(), c.dbname);
        }).iterator();
    }

    public void init() {
        descriptors.forEach((id, desc) -> {
            log.debug("Initializing MongoClient with id={}", id);
            clients.put(id, MongoDBConnectionHelper.newMongoClient(desc));
        });
    }

}
