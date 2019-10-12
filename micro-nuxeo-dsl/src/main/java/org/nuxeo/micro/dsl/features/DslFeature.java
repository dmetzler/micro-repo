package org.nuxeo.micro.dsl.features;

import java.util.Map;

import org.nuxeo.micro.dsl.DslModel;

public interface DslFeature {

    void visit(DslModel model, Map<String, Object> ast);

}
