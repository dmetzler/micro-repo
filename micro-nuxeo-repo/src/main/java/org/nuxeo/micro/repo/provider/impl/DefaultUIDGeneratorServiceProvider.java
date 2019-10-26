package org.nuxeo.micro.repo.provider.impl;

import org.nuxeo.ecm.core.uidgen.UIDGeneratorService;
import org.nuxeo.ecm.core.uidgen.UIDGeneratorServiceImpl;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;

public class DefaultUIDGeneratorServiceProvider implements UIDGeneratorServiceProvider {

    @Override
    public UIDGeneratorService getForTenant(String tenantId) {
        UIDGeneratorServiceImpl result = new UIDGeneratorServiceImpl();
        result.start();
        return result;
    }

}
