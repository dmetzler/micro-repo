package org.nuxeo.micro.repo.provider.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DslFeature;

public class DocumentTypeFeature implements DslFeature {

    private List<DocumentTypeDescriptor> doctypes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DslModel model, Map<String, Object> ast) {
        if (ast.get("doctypes") != null) {

            List<Map<String, Object>> doctypesMap = (List<Map<String, Object>>) ast.get("doctypes");

            doctypesMap.forEach(dt -> {
                DocumentTypeDescriptor descriptor = new DocumentTypeDescriptor();
                descriptor.name = (String) dt.get("name");
                descriptor.superTypeName = (String) dt.get("superTypeName");
                descriptor.facets = ((List<String>) dt.get("facets")).toArray(new String[0]);

                if (dt.containsKey("schemas")) {
                    List<SchemaDescriptor> sds = new ArrayList<>();
                    ((List<Map<String, Object>>) dt.get("schemas")).forEach(s -> {
                        SchemaDescriptor sd = new SchemaDescriptor();
                        sd.name = (String) s.get("name");
                        sd.isLazy = (boolean) s.get("isLazy");
                        sds.add(sd);
                    });

                    descriptor.schemas = sds.toArray(new SchemaDescriptor[0]);
                } else {
                    descriptor.schemas = new SchemaDescriptor[0];
                }
                doctypes.add(descriptor);
            });

        }
    }

    public List<DocumentTypeDescriptor> getDocTypes() {
        return doctypes;
    }

}
