package org.nuxeo.micro.repo;

import org.nuxeo.ecm.core.CoreService;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.CoreSessionServiceImpl;
import org.nuxeo.ecm.core.api.validation.DocumentValidationServiceImpl;
import org.nuxeo.ecm.core.filter.CharacterFilteringServiceImpl;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.ecm.core.trash.PropertyTrashService;
import org.nuxeo.ecm.core.versioning.VersioningServiceImpl;
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
            VersioningServiceImpl versioningService = new VersioningServiceImpl(repository.getSchemaManager(), coreSessionService, eventService);
            DocumentValidationServiceImpl documentValidationService = new DocumentValidationServiceImpl(repository.getSchemaManager());
            PropertyTrashService trashService = new PropertyTrashService(eventService);

            coreSessionService = CoreSessionServiceImpl.builder()//
                    .repository(repository)//
                    .eventService(eventService) //
                    .securityService(new SecurityService()) //
                    .versioningService(versioningService)//
                    .lifeCycleService(new MockLifeCycleServiceImpl())//
                    .trashService(trashService)//
                    .documentValidationService(documentValidationService)
                    .charFilteringService(new CharacterFilteringServiceImpl())//
                    .coreService(new CoreService())//
                    .build();
        }
        return coreSessionService;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Repository getRepository() {
        return repository;
    }

}
