/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.ecm.platform.el;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for the service providing access to EL-related functions.
 *
 * @since 8.3
 */
public class ELServiceServiceImpl implements ELService {

    private static final Logger log = LoggerFactory.getLogger(ELServiceServiceImpl.class);


    protected static final ELContextFactory DEFAULT_EL_CONTEXT_FACTORY = new DefaultELContextFactory();

    protected List<ELContextFactoryDescriptor> elContextFactoryDescriptors;

    protected ELContextFactory elContextFactory = DEFAULT_EL_CONTEXT_FACTORY;

    public ELServiceServiceImpl() {
        elContextFactoryDescriptors = new ArrayList<>(1);
    }

    public void registerContribution(ELContextFactoryDescriptor contribution) {
        log.info("Registered ELContextFactory: " + contribution.klass.getName());
        registerELContextFactoryDescriptor(contribution);
    }

    public void registerELContextFactoryDescriptor(ELContextFactoryDescriptor desc) {
        elContextFactoryDescriptors.add(desc);
        elContextFactory = desc.newInstance();
    }


    @Override
    public ELContext createELContext() {
        return elContextFactory.get();
    }

}
