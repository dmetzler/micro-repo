/*
 * (C) Copyright 2006-2017 Nuxeo (http://nuxeo.com/) and others.
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
 *     Bogdan Stefanescu
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component and service managing low-level repository instances.
 */
public class RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);

    private RepositoryManager repositoryManager;

    public RepositoryService(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    private final Map<String, Repository> repositories = new ConcurrentHashMap<>();

    public void shutdown() {
        log.info("Shutting down repository manager");
        repositories.values().forEach(Repository::shutdown);
        repositories.clear();
    }

    public void start() {
        TransactionHelper.runInTransaction(this::doCreateRepositories);
    }

    public void stop() {
        TransactionHelper.runInTransaction(this::shutdown);
    }

    /**
     * Start a tx and initialize repositories content. This method is publicly exposed since it is needed by tests to
     * initialize repositories after cleanups (see CoreFeature).
     *
     * @since 8.4
     */
    public void initRepositories() {
        TransactionHelper.runInTransaction(this::doInitRepositories);
    }

    /**
     * Creates all the repositories. Requires an active transaction.
     *
     * @since 9.3
     */
    public void doCreateRepositories() {
        repositories.clear();
        for (String repositoryName : getRepositoryNames()) {
            RepositoryFactory factory = getFactory(repositoryName);
            if (factory == null) {
                continue;
            }
            Repository repository = (Repository) factory.call();
            repositories.put(repositoryName, repository);
        }
    }

    /**
     * Initializes all the repositories. Requires an active transaction.
     *
     * @since 9.3
     */
    protected void doInitRepositories() {
        // give up if no handler configured
        RepositoryInitializationHandler handler = RepositoryInitializationHandler.getInstance();
        if (handler == null) {
            return;
        }
        // invoke handlers
//        for (String name : getRepositoryNames()) {
//            initializeRepository(handler, name);
//        }
    }

//    protected void initializeRepository(final RepositoryInitializationHandler handler, String name) {
//        new UnrestrictedSessionRunner(name) {
//            @Override
//            public void run() {
//                handler.initializeRepository(session);
//            }
//        }.runUnrestricted();
//    }

    /**
     * Gets a repository given its name.
     * <p>
     * Null is returned if no repository with that name was registered.
     *
     * @param repositoryName the repository name
     * @return the repository instance or null if no repository with that name was registered
     */
    public Repository getRepository(String repositoryName) {
        return repositories.get(repositoryName);
    }

    protected RepositoryFactory getFactory(String repositoryName) {
        org.nuxeo.ecm.core.api.repository.Repository repo = repositoryManager.getRepository(repositoryName);
        if (repo == null) {
            return null;
        }
        RepositoryFactory repositoryFactory = (RepositoryFactory) repo.getRepositoryFactory();
        if (repositoryFactory == null) {
            throw new NullPointerException("Missing repositoryFactory for repository: " + repositoryName);
        }
        return repositoryFactory;
    }

    public List<String> getRepositoryNames() {
        return repositoryManager.getRepositoryNames();
    }

}
