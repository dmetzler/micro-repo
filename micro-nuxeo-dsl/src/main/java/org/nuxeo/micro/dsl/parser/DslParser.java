package org.nuxeo.micro.dsl.parser;

import org.nuxeo.micro.dsl.DslModel;

public interface DslParser {

    DslModel parse(String dsl);
}
