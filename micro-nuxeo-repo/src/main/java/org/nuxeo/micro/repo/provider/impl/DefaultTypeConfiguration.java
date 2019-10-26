package org.nuxeo.micro.repo.provider.impl;

import org.nuxeo.ecm.core.schema.TypeConfiguration;

public class DefaultTypeConfiguration extends TypeConfiguration {

    public DefaultTypeConfiguration() {
        this.prefetchInfo = "common, dublincore";
        this.clearComplexPropertyBeforeSet = true;
        this.allowVersionWriteForDublinCore = false;
    }
}
