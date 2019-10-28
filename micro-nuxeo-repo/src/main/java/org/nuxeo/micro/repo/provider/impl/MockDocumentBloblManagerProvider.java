package org.nuxeo.micro.repo.provider.impl;

import org.nuxeo.ecm.core.blob.DocumentBlobManager;
import org.nuxeo.ecm.core.blob.DocumentBlobManagerImpl;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;

public class MockDocumentBloblManagerProvider implements DocumentBlobManagerProvider {

    @Override
    public DocumentBlobManager getForTenant(String tenantId) {
        return new DocumentBlobManagerImpl(null);
    }

}
