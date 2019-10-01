/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.ecm.core.api.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level service to get to a {@link org.nuxeo.ecm.core.api.repository.Repository Repository} and from there to
 * {@link org.nuxeo.ecm.core.api.CoreSession CoreSession} objects.
 */
public class RepositoryManagerImpl implements RepositoryManager {

    private static final Logger log = LoggerFactory.getLogger(RepositoryManagerImpl.class);

    private Map<String, Repository> repositories = Collections.synchronizedMap(new LinkedHashMap<String, Repository>());

    // compat from old extension point
    private Map<String, Repository> compatRepositories = new ConcurrentHashMap<String, Repository>();

    // compat
    private static final String XP_REPOSITORIES = "repositories";

    // called by low-level repository service
    @Override
    public void addRepository(Repository repository) {
        String name = repository.getName();
        if (repositories.containsKey(name)) {
            log.info("Overriding repository: " + name);
        } else {
            log.info("Registering repository: " + name);
        }
        Repository compat = compatRepositories.get(name);
        if (compat != null) {
            if (repository.getLabel() == null) {
                repository.setLabel(compat.getLabel());
            }
            if (repository.getDefault() != null) {
                repository.setDefault(compat.getDefault());
            }
        }
        repositories.put(name, repository);
    }

    // call by low-level repository service
    @Override
    public void removeRepository(String name) {
        log.info("Removing repository: {}", name);
        repositories.remove(name);
    }

    @Override
    public Collection<Repository> getRepositories() {
        return new ArrayList<Repository>(repositories.values());
    }

    @Override
    public List<String> getRepositoryNames() {
        return new ArrayList<String>(repositories.keySet());
    }

    @Override
    public Repository getRepository(String name) {
        return repositories.get(name);
    }

    @Override
    public Repository getDefaultRepository() {
        for (Repository repository : repositories.values()) {
            if (repository.isDefault()) {
                return repository;
            }
            if ("default".equals(repository.getName())) {
                return repository;
            }
        }
        // fallback to first in list
        if (!repositories.isEmpty()) {
            return repositories.values().iterator().next();
        }
        // no repository at all
        throw new RuntimeException("No repository defined");
    }

    @Override
    public String getDefaultRepositoryName() {
        return getDefaultRepository().getName();
    }

}
