package org.nuxeo.micro.repo.provider.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DslFeature;

public class SchemaFeature implements DslFeature {

    List<SchemaBindingDescriptor> descriptors = new ArrayList<>(0);

    Map<String, FieldsDef> fieldsDefs = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DslModel model, Map<String, Object> ast) {
        if (ast.get("schemas") != null) {

            List<Map<String, Object>> schemas = (List<Map<String, Object>>) ast.get("schemas");

            for (Map<String, Object> schemaDef : schemas) {

                Map<String, String> desc = (Map<String, String>) schemaDef.get("descriptor");

                SchemaBindingDescriptor descriptor = new SchemaBindingDescriptor();
                descriptor.name = desc.get("name");
                descriptor.prefix = desc.get("prefix");
                descriptors.add(descriptor);

                List<Map<String, String>> fields = (List<Map<String, String>>) schemaDef.get("fields");
                fieldsDefs.put(descriptor.name, FieldsDef.fromAst(fields));
            }

        }
    }

    public List<SchemaBindingDescriptor> getShemaBindings() {
        return descriptors;
    }

    public Map<String, FieldsDef> getFieldDefs() {
        return fieldsDefs;
    }

    public static class FieldsDef {
        private List<Field> fields = new ArrayList<>();

        public static FieldsDef fromAst(List<Map<String, String>> defs) {
            FieldsDef fdef = new FieldsDef();
            for (Map<String, String> def : defs) {
                Field field = new Field(def.get("name"), def.get("type"));
                fdef.fields.add(field);
            }
            return fdef;
        }

        public List<Field> getFields() {
            return fields;
        }

    }

    public static class Field {
        private String name;

        private String type;

        public Field(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
