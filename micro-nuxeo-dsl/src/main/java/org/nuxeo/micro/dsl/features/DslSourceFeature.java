package org.nuxeo.micro.dsl.features;

import java.util.Map;

import org.nuxeo.micro.dsl.DslModel;

public class DslSourceFeature implements DslFeature {

    private String src;

    @Override
    public void visit(DslModel model, Map<String, Object> ast) {
        src = (String) ast.get("src");
    }

    public String getSrc() {
        return src;
    }

}
