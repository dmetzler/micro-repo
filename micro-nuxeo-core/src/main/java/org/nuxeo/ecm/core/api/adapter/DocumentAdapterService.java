/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.core.api.adapter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class DocumentAdapterService {
    private static final Logger log = LoggerFactory.getLogger(DocumentAdapterService.class);

    /**
     * Document adapters
     */
    protected Map<Class<?>, DocumentAdapterDescriptor> adapters = new ConcurrentHashMap<Class<?>, DocumentAdapterDescriptor>();;

    public DocumentAdapterDescriptor getAdapterDescriptor(Class<?> itf) {
        return adapters.get(itf);
    }

    /**
     * @since 5.7
     */
    public DocumentAdapterDescriptor[] getAdapterDescriptors() {
        Collection<DocumentAdapterDescriptor> values = adapters.values();
        return values.toArray(new DocumentAdapterDescriptor[values.size()]);
    }

    public void registerAdapterFactory(DocumentAdapterDescriptor dae) {
        adapters.put(dae.getInterface(), dae);
        log.info("Registered document adapter factory " + dae);
    }

    public void unregisterAdapterFactory(Class<?> itf) {
        DocumentAdapterDescriptor dae = adapters.remove(itf);
        if (dae != null) {
            log.info("Unregistered document adapter factory: " + dae);
        }
    }

}
