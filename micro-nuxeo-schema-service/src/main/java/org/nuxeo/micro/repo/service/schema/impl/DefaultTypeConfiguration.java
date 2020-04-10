package org.nuxeo.micro.repo.service.schema.impl;

import org.nuxeo.ecm.core.schema.TypeConfiguration;

public class DefaultTypeConfiguration extends TypeConfiguration {

    private static final long serialVersionUID = 1L;

    public DefaultTypeConfiguration() {
        this.prefetchInfo = "common, dublincore";
        this.clearComplexPropertyBeforeSet = true;
        this.allowVersionWriteForDublinCore = false;
    }
}
