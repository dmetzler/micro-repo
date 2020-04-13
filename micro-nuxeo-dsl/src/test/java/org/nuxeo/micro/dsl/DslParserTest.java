package org.nuxeo.micro.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.micro.dsl.features.DslSourceFeature;
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

        String src = "doctype NewType { schemas { common dublincore custom { one two }} crud facets {Folderish}}";

        Map<String, Object> ast = dslparser.getAbstractSyntaxTree(src);

        DslModel model = DslModel.builder().with(DslSourceFeature.class, DoctypeCounterFeature.class).build();
        model.visit(ast);

        DslSourceFeature feature = model.getFeature(DslSourceFeature.class);

        assertThat(feature.getSrc()).isEqualTo(src);

        DoctypeCounterFeature countFeature = model.getFeature(DoctypeCounterFeature.class);
        assertThat(countFeature.getCount()).isEqualTo(1);

        printMap(ast, "");

    }

    private void printMap(Map<String, Object> ast, String prefix) {
        for (Entry<String, Object> entry : ast.entrySet()) {

            System.out.println(String.format("%s%s -> %s", prefix, entry.getKey(),
                    entry.getValue().getClass().getCanonicalName()));

            if (entry.getValue() instanceof ArrayList) {
                printList((List) entry.getValue(), prefix + " ");
            } else if (entry.getValue() instanceof HashMap) {
                printMap((Map<String, Object>) entry.getValue(), prefix + " ");
            }

        }

    }

    private void printList(List list, String prefix) {
        for (Object o : list) {
            if (o instanceof ArrayList) {
                printList((List) o, prefix + " ");
            } else if (o instanceof HashMap) {
                printMap((Map<String, Object>) o, prefix + " ");
            } else {
                System.out.println(prefix + o);
            }
        }
    }

}
