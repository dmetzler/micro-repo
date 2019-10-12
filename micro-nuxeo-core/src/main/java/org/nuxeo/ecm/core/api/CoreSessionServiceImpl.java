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
package org.nuxeo.ecm.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nuxeo.ecm.core.CoreService;
import org.nuxeo.ecm.core.api.local.LocalSession;
import org.nuxeo.ecm.core.api.trash.TrashService;
import org.nuxeo.ecm.core.api.validation.DocumentValidationService;
import org.nuxeo.ecm.core.api.versioning.VersioningService;
import org.nuxeo.ecm.core.filter.CharacterFilteringService;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.micro.external.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Implementation for the service managing the acquisition/release of {@link CoreSession} instances.
 *
 * @since 8.4
 */
public class CoreSessionServiceImpl implements CoreSessionService {

    private static final Logger log = LoggerFactory.getLogger(CoreSessionServiceImpl.class);

    /**
     * All open {@link CoreSessionRegistrationInfo}, keyed by session id.
     */
    private final Map<String, CoreSessionRegistrationInfo> sessions = new ConcurrentHashMap<>();

    protected final SecurityService securityService;

    protected final VersioningService versioningService;

    protected final DocumentValidationService documentValidationService;

    protected final EventService eventService;

    protected final CharacterFilteringService charFilteringService;

    protected final TrashService trashService;

    protected final LifeCycleService lifeCycleService;

    protected final CoreService coreService;

    private CoreSessionServiceImpl(SecurityService securityService, VersioningService versioningService,
            DocumentValidationService documentValidationService, EventService eventService,
            CharacterFilteringService charFilteringService, TrashService trashService,
            LifeCycleService lifeCycleService, CoreService coreService) {

        this.securityService = securityService;

        this.versioningService = versioningService;

        this.documentValidationService = documentValidationService;


        this.eventService = eventService;

        this.charFilteringService = charFilteringService;

        this.trashService = trashService;

        this.lifeCycleService = lifeCycleService;

        this.coreService = coreService;

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected SecurityService securityService;

        protected VersioningService versioningService;

        protected DocumentValidationService documentValidationService;

        protected EventService eventService;

        protected CharacterFilteringService charFilteringService;

        protected TrashService trashService;

        protected LifeCycleService lifeCycleService;

        protected CoreService coreService;

        protected Builder() {

        }

        public Builder securityService(SecurityService securityService) {
            this.securityService = securityService;
            return this;
        }

        public Builder versioningService(VersioningService versioningService) {
            this.versioningService = versioningService;
            return this;
        }

        public Builder documentValidationService(DocumentValidationService documentValidationService) {
            this.documentValidationService = documentValidationService;
            return this;
        }

        public Builder eventService(EventService eventService) {
            this.eventService = eventService;
            return this;
        }

        public Builder charFilteringService(CharacterFilteringService charFilteringService) {
            this.charFilteringService = charFilteringService;
            return this;
        }

        public Builder trashService(TrashService trashService) {
            this.trashService = trashService;
            return this;
        }

        public Builder lifeCycleService(LifeCycleService lifeCycleService) {
            this.lifeCycleService = lifeCycleService;
            return this;
        }

        public Builder coreService(CoreService coreService) {
            this.coreService = coreService;
            return this;
        }

        public CoreSessionServiceImpl build() {
            return new CoreSessionServiceImpl(securityService, versioningService, documentValidationService,
                    eventService, charFilteringService, trashService, lifeCycleService, coreService);
        }

    }

    /**
     * Most recently closed sessions.
     */
    protected final Cache<String, CoreSessionRegistrationInfo> recentlyClosedSessions = //
            CacheBuilder.newBuilder().maximumSize(100).build();

    @Override
    public CloseableCoreSession createCoreSession(Repository repository, NuxeoPrincipal principal) {
        LocalSession session = new LocalSession(repository, principal, this);
        sessions.put(session.getSessionId(), new CoreSessionRegistrationInfo(session));
        return session;
    }

    @Override
    public void releaseCoreSession(CloseableCoreSession session) {
        String sessionId = session.getSessionId();
        CoreSessionRegistrationInfo info = sessions.remove(sessionId);
        String debug = "closing stacktrace, sessionId=" + sessionId + ", thread=" + Thread.currentThread().getName();
        if (info == null) {
            CoreSessionRegistrationInfo closed = recentlyClosedSessions.getIfPresent(sessionId);
            if (closed == null) {
                // no knowledge of this sessionId, log the current stacktrace
                Exception e = new Exception("DEBUG: " + debug);
                log.warn("Closing unknown CoreSession", e);
            } else {
                // this sessionId was recently closed and we kept info about it
                // log the current stacktrace with the original opening and closing as suppressed exceptions
                Exception e = new Exception("DEBUG: spurious " + debug);
                e.addSuppressed(closed);
                log.warn("Closing already closed CoreSession", e);
            }
        } else {
            // regular closing, record a stacktrace
            info.addSuppressed(new Exception("DEBUG: " + debug));
            recentlyClosedSessions.put(sessionId, info);
            // don't keep the session around, all we want is the stacktrace objects
            info.session = null;
        }
        session.destroy();
    }

    @Override
    public CoreSession getCoreSession(String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("null sessionId");
        }
        CoreSessionRegistrationInfo info = sessions.get(sessionId);
        return info == null ? null : info.getCoreSession();
    }

    @Override
    public int getNumberOfOpenCoreSessions() {
        return sessions.size();
    }

    @Override
    public List<CoreSessionRegistrationInfo> getCoreSessionRegistrationInfos() {
        return new ArrayList<>(sessions.values());
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public VersioningService getVersioningService() {
        return versioningService;
    }

    public DocumentValidationService getDocumentValidationService() {
        return documentValidationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public CharacterFilteringService getCharFilteringService() {
        return charFilteringService;
    }

    public TrashService getTrashService() {
        return trashService;
    }

    public LifeCycleService getLifeCycleService() {
        return lifeCycleService;
    }

    public CoreService getCoreService() {
        return coreService;
    }

}
