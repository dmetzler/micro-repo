package org.nuxeo.micro.repo;

import static org.mockito.Mockito.mock;

import org.nuxeo.ecm.core.CoreService;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.CoreSessionServiceImpl;
import org.nuxeo.ecm.core.api.trash.TrashService;
import org.nuxeo.ecm.core.api.validation.DocumentValidationService;
import org.nuxeo.ecm.core.api.versioning.VersioningService;
import org.nuxeo.ecm.core.filter.CharacterFilteringService;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.micro.event.EventService;

public class RepoConfiguration {

    private CoreSessionService coreSessionService;

    private final Repository repository;
    private final String tenantId;
    private final EventService eventService;

    public RepoConfiguration(String tenantId, Repository repository, EventService eventService) {
        this.tenantId = tenantId;
        this.repository = repository;
        this.eventService = eventService;
    }

    public CoreSessionService getCoreSessionService() {
        if (coreSessionService == null) {
            coreSessionService = CoreSessionServiceImpl.builder()//
                    .repository(repository)//
                    .eventService(eventService) //
                    .securityService(new SecurityService()) //
                    .versioningService(mock(VersioningService.class)).lifeCycleService(mock(LifeCycleService.class))//
                    .trashService(mock(TrashService.class))//
                    .documentValidationService(mock(DocumentValidationService.class))
                    .charFilteringService(mock(CharacterFilteringService.class))//
                    .coreService(new CoreService())//
                    .build();
        }
        return coreSessionService;
    }

    public String getTenantId() {
        return tenantId;
    }

}
