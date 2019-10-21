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
 *     Dragos Mihalache
 */
package org.nuxeo.ecm.core.uidgen;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;

/**
 * Service that writes MetaData.
 */
public class UIDGeneratorComponent implements UIDGeneratorService {

    public static final String ID = "org.nuxeo.ecm.core.uidgen.UIDGeneratorService";

    public static final String UID_GENERATORS_EXTENSION_POINT = "generators";

    public static final String SEQUENCERS_EXTENSION_POINT = "sequencers";

    /**
     * Extension point is deprecated should be removed - preserved for now only for
     * startup warnings.
     */
    public static final String EXTENSION_POINT_SEQUENCER_FACTORY = "sequencerFactory";

    private static final Log log = LogFactory.getLog(UIDGeneratorComponent.class);

    protected final Map<String, UIDGenerator> generators = new HashMap<>();

    protected final Map<String, UIDSequencer> sequencers = new HashMap<>();

    protected final LinkedHashMap<String, UIDSequencerProviderDescriptor> sequencerContribs = new LinkedHashMap<>();

    protected String defaultSequencer;

    public void start() {
        for (String name : sequencers.keySet()) {
            sequencers.get(name).init();
        }
    }

    public void stop() {
        for (String name : sequencers.keySet()) {
            sequencers.get(name).dispose();
        }
    }

    protected void computeDefault() {
        String def = null;
        String last = null;
        for (UIDSequencerProviderDescriptor contrib : sequencerContribs.values()) {
            if (contrib.isIsdefault()) {
                def = contrib.getName();
            }
            last = contrib.getName();
        }

        if (def == null) {
            def = last;
        }
        defaultSequencer = def;
    }

    public void registerSequencers(final Object[] contribs) {
        for (Object contrib : contribs) {
            UIDSequencerProviderDescriptor seqDescriptor = (UIDSequencerProviderDescriptor) contrib;
            String name = seqDescriptor.getName();

            try {
                if (seqDescriptor.isEnabled()) {
                    UIDSequencer seq = seqDescriptor.getSequencer();
                    if (seq != null) {
                        seq.setName(name);
                    }
                    sequencers.put(name, seq);
                    sequencerContribs.put(name, seqDescriptor);

                } else {
                    log.info(String.format("Sequencer %s is disabled.", name));
                }
            } catch (Exception e) {
                log.error("Unable to create UIDSequencer with name " + name, e);
            }
        }
    }

    public void registerGenerators(final Object[] contribs) {

        // read the list of generators
        for (Object contrib : contribs) {
            final UIDGeneratorDescriptor generatorDescriptor = (UIDGeneratorDescriptor) contrib;
            final String generatorName = generatorDescriptor.getName();

            UIDGenerator generator;
            try {
                generator = (UIDGenerator) this.getClass().getClassLoader()
                        .loadClass(generatorDescriptor.getClassName()).getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            final String[] propNames = generatorDescriptor.getPropertyNames();
            if (propNames.length == 0) {
                log.error("no property name defined on generator " + generatorName);
            }
            // set the property name on generator
            generator.setPropertyNames(propNames);

            // Register Generator for DocTypes and property name
            final String[] docTypes = generatorDescriptor.getDocTypes();
            registerGeneratorForDocTypes(generator, docTypes);

            log.info("registered UID generator: " + generatorName);
        }
    }

    /**
     * Registers given UIDGenerator for the given document types. If there is
     * already a generator registered for one of document type it will be discarded
     * (and replaced with the new generator).
     */
    private void registerGeneratorForDocTypes(final UIDGenerator generator, final String[] docTypes) {

        for (String docType : docTypes) {
            final UIDGenerator previous = generators.put(docType, generator);
            if (previous != null) {
                log.info("Overwriting generator: " + previous.getClass() + " for docType: " + docType);
            }
            log.info("Registered generator: " + generator.getClass() + " for docType: " + docType);
        }
    }

    /**
     * Returns the uid generator to use for this document.
     * <p>
     * Choice is made following the document type and the generator configuration.
     */
    @Override
    public UIDGenerator getUIDGeneratorFor(DocumentModel doc) {
        final String docTypeName = doc.getType();
        final UIDGenerator generator = generators.get(docTypeName);

        if (generator == null) {
            log.debug("No UID Generator defined for doc type: " + docTypeName);
            return null;
        }
        // TODO maybe maintain an initialization state for generators
        // so the next call could be avoided (for each request)
        generator.setSequencer(getSequencer());

        return generator;
    }

    /**
     * Creates a new UID for the given doc and sets the field configured in the
     * generator component with this value.
     */
    @Override
    public void setUID(DocumentModel doc) throws PropertyNotFoundException {
        final UIDGenerator generator = getUIDGeneratorFor(doc);
        if (generator != null) {
            generator.setUID(doc);
        }
    }

    /**
     * @return a new UID for the given document
     */
    @Override
    public String createUID(DocumentModel doc) {
        final UIDGenerator generator = getUIDGeneratorFor(doc);
        if (generator == null) {
            return null;
        } else {
            return generator.createUID(doc);
        }
    }

    @Override
    public UIDSequencer getSequencer() {
        return getSequencer(null);
    }

    @Override
    public UIDSequencer getSequencer(String name) {
        if (name == null) {
            name = defaultSequencer;
        }
        return sequencers.get(name);
    }

}
