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
 *     Florent Guillaume
 *     Laurent Doguin
 */
package org.nuxeo.ecm.core.versioning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.versioning.VersioningService;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.micro.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Versioning service component and implementation.
 */
public class VersioningServiceImpl implements VersioningService {

    private static final Logger log = LoggerFactory.getLogger(VersioningServiceImpl.class);

    public static final String VERSIONING_SERVICE_XP = "versioningService";

    public static final String VERSIONING_RULE_XP = "versioningRules";

    public static final String VERSIONING_POLICY_XP = "policies";

    public static final String VERSIONING_FILTER_XP = "filters";

    public static final String VERSIONING_RESTRICTION_XP = "restrictions";

    protected Map<VersioningServiceDescriptor, VersioningService> versioningServices = new LinkedHashMap<>();

    protected VersioningPolicyRegistry versioningPoliciesRegistry = new VersioningPolicyRegistry();

    protected VersioningFilterRegistry versioningFiltersRegistry = new VersioningFilterRegistry();

    protected VersioningRestrictionRegistry versioningRestrictionsRegistry = new VersioningRestrictionRegistry();

    protected static class VersioningPolicyRegistry extends HashMap<String, VersioningPolicyDescriptor> {

        private static final long serialVersionUID = 1L;

        public Map<String, VersioningPolicyDescriptor> getVersioningPolicyDescriptors() {
            return entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }

        public void addContribution(VersioningPolicyDescriptor contrib) {
            put(contrib.getId(), contrib);

        }

    }

    protected static class VersioningFilterRegistry extends HashMap<String, VersioningFilterDescriptor> {

        private static final long serialVersionUID = 1L;

        public void addContribution(VersioningFilterDescriptor contrib) {
            put(contrib.getId(), contrib);
        }

        public Map<String, VersioningFilterDescriptor> getVersioningFilterDescriptors() {
            return this;
        }

    }

    protected static class VersioningRestrictionRegistry extends HashMap<String, VersioningRestrictionDescriptor> {

        private static final long serialVersionUID = 1L;

        public void addContribution(VersioningRestrictionDescriptor contrib) {
            put(contrib.getType(), contrib);
        }

        public Map<String, VersioningRestrictionDescriptor> getVersioningRestrictionDescriptors() {
            return this;
        }

    }

    // public for tests
    public VersioningService service = null;

    private VersioningService standardService;

    public VersioningServiceImpl(SchemaManager schemaManager, CoreSessionService css, EventService eventService) {
        this.standardService = new StandardVersioningService(schemaManager, css, eventService);
        recompute();
    }


    protected void registerVersioningService(VersioningServiceDescriptor contrib) {
        String klass = contrib.className;
        try {
            VersioningService vs = (VersioningService) VersioningServiceImpl.class.getClassLoader().loadClass(klass)
                    .getDeclaredConstructor().newInstance();
            versioningServices.put(contrib, vs);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate: " + klass, e);
        }
        log.info("Registered versioning service: " + klass);
        recompute();
    }

    protected void unregisterVersioningService(VersioningServiceDescriptor contrib) {
        versioningServices.remove(contrib);
        log.info("Unregistered versioning service: " + contrib.className);
        recompute();
    }

    protected void registerVersioningPolicy(VersioningPolicyDescriptor contrib) {
        versioningPoliciesRegistry.addContribution(contrib);
        log.info("Registered versioning policy: " + contrib.getId());
        recompute();
    }

    protected void registerVersioningFilter(VersioningFilterDescriptor contrib) {
        versioningFiltersRegistry.addContribution(contrib);
        log.info("Registered versioning filter: " + contrib.getId());
        recompute();
    }

    protected void registerVersioningRestriction(VersioningRestrictionDescriptor contrib) {
        versioningRestrictionsRegistry.addContribution(contrib);
        log.info("Registered versioning restriction: " + contrib.getType());
        recompute();
    }

    protected void recompute() {
        VersioningService versioningService = standardService;
        for (VersioningService vs : versioningServices.values()) {
            versioningService = vs;
        }
        if (versioningService instanceof ExtendableVersioningService) {
            ExtendableVersioningService evs = (ExtendableVersioningService) versioningService;
            evs.setVersioningPolicies(getVersioningPolicies());
            evs.setVersioningFilters(getVersioningFilters());
            evs.setVersioningRestrictions(getVersioningRestrictions());
        }
        this.service = versioningService;
    }

    protected Map<String, VersioningPolicyDescriptor> getVersioningPolicies() {
        return versioningPoliciesRegistry.getVersioningPolicyDescriptors();
    }

    protected Map<String, VersioningFilterDescriptor> getVersioningFilters() {
        return versioningFiltersRegistry.getVersioningFilterDescriptors();
    }

    protected Map<String, VersioningRestrictionDescriptor> getVersioningRestrictions() {
        return versioningRestrictionsRegistry.getVersioningRestrictionDescriptors();
    }

    @Override
    public String getVersionLabel(DocumentModel doc) {
        return service.getVersionLabel(doc);
    }

    @Override
    public void doPostCreate(Document doc, Map<String, Serializable> options) {
        service.doPostCreate(doc, options);
    }

    @Override
    public List<VersioningOption> getSaveOptions(DocumentModel docModel) {
        return service.getSaveOptions(docModel);
    }

    @Override
    public boolean isPreSaveDoingCheckOut(Document doc, boolean isDirty, VersioningOption option,
            Map<String, Serializable> options) {
        return service.isPreSaveDoingCheckOut(doc, isDirty, option, options);
    }

    @Override
    public VersioningOption doPreSave(CoreSession session, Document doc, boolean isDirty, VersioningOption option,
            String checkinComment, Map<String, Serializable> options) {
        return service.doPreSave(session, doc, isDirty, option, checkinComment, options);
    }

    @Override
    public boolean isPostSaveDoingCheckIn(Document doc, VersioningOption option, Map<String, Serializable> options) {
        return service.isPostSaveDoingCheckIn(doc, option, options);
    }

    @Override
    public Document doPostSave(CoreSession session, Document doc, VersioningOption option, String checkinComment,
            Map<String, Serializable> options) {
        return service.doPostSave(session, doc, option, checkinComment, options);
    }

    @Override
    public Document doCheckIn(Document doc, VersioningOption option, String checkinComment) {
        return service.doCheckIn(doc, option, checkinComment);
    }

    @Override
    public void doCheckOut(Document doc) {
        service.doCheckOut(doc);
    }

    @Override
    public void doAutomaticVersioning(DocumentModel previousDocument, DocumentModel currentDocument, boolean before) {
        service.doAutomaticVersioning(previousDocument, currentDocument, before);
    }

}
