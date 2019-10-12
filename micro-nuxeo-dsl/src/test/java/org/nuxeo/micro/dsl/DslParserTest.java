package org.nuxeo.micro.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.micro.dsl.features.DocumentTypeFeature;
import org.nuxeo.micro.dsl.parser.DslParser;
import org.nuxeo.micro.dsl.parser.DslParserImpl;

public class DslParserTest {

    protected DslParser dslparser;

    @Before
    public void doBefore() {
        dslparser = new DslParserImpl();
        ((DslParserImpl) dslparser).init();
    }

    @Test
    public void it_can_parse_a_dsl() throws Exception {

        DslModel model = dslparser.parse("doctype NewType { schemas { common dublincore } facets {Folderish}}");
        DocumentTypeFeature feature = model.getFeature(DocumentTypeFeature.class);

        assertThat(feature.getDocTypes()).hasSize(1);

        DocumentTypeDescriptor descriptor = feature.getDocTypes().get(0);
        assertThat(descriptor.name).isEqualTo("NewType");
        assertThat(descriptor.superTypeName).isEqualTo("Document");

    }
}
