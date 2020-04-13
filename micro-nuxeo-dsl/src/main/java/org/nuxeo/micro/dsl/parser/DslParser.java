package org.nuxeo.micro.dsl.parser;

import java.util.Map;

import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DslFeature;

public interface DslParser {

    Map<String, Object> getAbstractSyntaxTree(String dsl);

    @SuppressWarnings("unchecked")
    DslModel parse(String dsl, Class<? extends DslFeature>... dslFeatures);
}
