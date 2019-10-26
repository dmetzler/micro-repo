package org.nuxeo.micro.repo.provider.impl;
import static org.mockito.Mockito.mock;

import org.nuxeo.ecm.core.blob.DocumentBlobManager;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;

public class MockDocumentBloblManagerProvider implements DocumentBlobManagerProvider{

    @Override
    public DocumentBlobManager getForTenant(String tenantId) {
        return mock(DocumentBlobManager.class);
    }

}
