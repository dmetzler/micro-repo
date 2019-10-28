package org.nuxeo.ecm.core.api.impl;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;

public class SerializableSchemaManagerTest {
    private List<String> schemas = Arrays.asList(new String[] {  "common", "uid", "dublincore" });

    @Test
    public void schema_manager_can_be_serialized() throws Exception {

        SchemaManagerImpl sm = new SchemaManagerImpl(FileUtils.getTempDirectory());
        buildSchemasAndTypes(sm);

        byte[] serializedSm;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos)) {
            out.writeObject(sm);
            serializedSm = baos.toByteArray();
        }

        try(ByteArrayInputStream bais= new ByteArrayInputStream(serializedSm);
            ObjectInputStream in = new ObjectInputStream(bais) ) {
            sm = (SchemaManagerImpl)in.readObject();
        }
        System.out.println(serializedSm.length);
        assertTrue(sm.getDocumentTypes().length > 0);
    }


    private void buildSchemasAndTypes(SchemaManagerImpl sm) {
        for (String schemaName : schemas) {
            SchemaBindingDescriptor sd = new SchemaBindingDescriptor(schemaName, schemaName);
            sd.src = schemaName + ".xsd";
            sd.prefix = "dublincore".equals(schemaName) ? "dc" : schemaName;
            sm.registerSchema(sd);
        }

        DocumentTypeDescriptor dtd = new DocumentTypeDescriptor("Document", "Root",
                getSchemaDescriptors("uid", "common"), new String[] { FacetNames.FOLDERISH });

        sm.registerDocumentType(dtd);

        dtd = new DocumentTypeDescriptor("Document", "Folder", getSchemaDescriptors("uid", "common", "dublincore"),
                new String[] {});
        sm.registerDocumentType(dtd);

        sm.flushPendingsRegistration();
    }

    public SchemaDescriptor[] getSchemaDescriptors(String... schemas) {
        SchemaDescriptor[] result = new SchemaDescriptor[schemas.length];
        for (int i = 0; i < schemas.length; i++) {
            String name = schemas[i];
            result[i] = new SchemaDescriptor(name);
        }
        return result;
    }

}
