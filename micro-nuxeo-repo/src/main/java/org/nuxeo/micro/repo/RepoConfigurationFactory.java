package org.nuxeo.micro.repo;

import org.nuxeo.ecm.core.model.DuplicatedNameFixer;
import org.nuxeo.ecm.core.model.EmptyNameFixer;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.uidgen.DocUIDGeneratorListener;
import org.nuxeo.ecm.platform.dublincore.listener.DublinCoreListener;
import org.nuxeo.micro.event.EventListenerDescriptor;
import org.nuxeo.micro.event.EventService;
import org.nuxeo.micro.event.impl.EventServiceImpl;
import org.nuxeo.micro.event.impl.InlineEventDescriptor;
import org.nuxeo.micro.repo.provider.RepositoryProvider;

public class RepoConfigurationFactory {

    protected final RepositoryProvider repositoryProvider;

    public RepoConfigurationFactory(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    /**
     * Return a repository configuration for a given tenant.
     *
     * @param tenantId
     * @return
     */
    public RepoConfiguration getConfiguration(String tenantId) {
        return new RepoConfiguration(tenantId, getRepositoryForTenant(tenantId), getEventServiceForTenant(tenantId));
    }

    private EventService getEventServiceForTenant(String tenantId) {
        EventServiceImpl service = new EventServiceImpl();

        EventListenerDescriptor docuidListener = InlineEventDescriptor.builder(DocUIDGeneratorListener.class)
                .on("documentCreated").withPriority(10).build();

        EventListenerDescriptor dublinCoreListener = InlineEventDescriptor
                .builder(DublinCoreListener.class).on("documentCreated", "aboutToCreate", "beforeDocumentModification",
                        "documentPublished", "lifecycle_transition_event", "documentCreatedByCopy")
                .withPriority(120).build();

        EventListenerDescriptor duplicateNameListener = InlineEventDescriptor.builder(DuplicatedNameFixer.class)
                .on("aboutToImport", "aboutToCreate", "aboutToMove").withPriority(1000).build();
        EventListenerDescriptor emptyNameListener = InlineEventDescriptor.builder(EmptyNameFixer.class)
                .on("aboutToImport", "aboutToCreate", "aboutToMove").withPriority(2000).build();

        service.addEventListener(docuidListener);
        service.addEventListener(dublinCoreListener);
        service.addEventListener(duplicateNameListener);
        service.addEventListener(emptyNameListener);

        return service;
    }

    private Repository getRepositoryForTenant(String tenantId) {
        return repositoryProvider.getForTenant(tenantId);
    }

}
