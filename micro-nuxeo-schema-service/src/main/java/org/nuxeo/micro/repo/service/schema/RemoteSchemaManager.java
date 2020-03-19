package org.nuxeo.micro.repo.service.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.PropertyDeprecationHandler;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.schema.types.CompositeType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class RemoteSchemaManager implements SchemaManager {
    private static final long serialVersionUID = 1L;

    private SchemaManager delegate;

    public RemoteSchemaManager(JsonObject json) {
        if (!SchemaManagerImpl.class.getCanonicalName().equals(json.getString("type"))) {
            throw new IllegalArgumentException("Can only decode " + SchemaManagerImpl.class.getCanonicalName());
        }
        byte[] data = Base64.getDecoder().decode(json.getString("value"));

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            delegate = (SchemaManager) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to deserialize JSON", e);
        }
    }

    public RemoteSchemaManager(SchemaManagerImpl sm) {
        delegate = sm;
    }

    public JsonObject toJson() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos)) {

            out.writeObject(delegate);
            String b64sm = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new JsonObject().put("type", delegate.getClass().getCanonicalName()).put("value", b64sm);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to serialize SchemaManager", e);
        }
    }

    public Field getField(String xpath) {
        return delegate.getField(xpath);
    }

    public Schema getSchema(String schema) {
        return delegate.getSchema(schema);
    }

    public Schema[] getSchemas() {
        return delegate.getSchemas();
    }

    public Field getField(Field field, String subFieldName) {
        return delegate.getField(field, subFieldName);
    }

    public DocumentType getDocumentType(String docType) {
        return delegate.getDocumentType(docType);
    }

    public DocumentType[] getDocumentTypes() {
        return delegate.getDocumentTypes();
    }

    public Schema getSchemaFromPrefix(String schemaPrefix) {
        return delegate.getSchemaFromPrefix(schemaPrefix);
    }

    public CompositeType getFacet(String name) {
        return delegate.getFacet(name);
    }

    public Schema getSchemaFromURI(String schemaURI) {
        return delegate.getSchemaFromURI(schemaURI);
    }

    public CompositeType[] getFacets() {
        return delegate.getFacets();
    }

    public Set<String> getDocumentTypeNamesForFacet(String facet) {
        return delegate.getDocumentTypeNamesForFacet(facet);
    }

    public Set<String> getNoPerDocumentQueryFacets() {
        return delegate.getNoPerDocumentQueryFacets();
    }

    public List<Schema> getProxySchemas(String docType) {
        return delegate.getProxySchemas(docType);
    }

    public Set<String> getDocumentTypeNamesExtending(String docType) {
        return delegate.getDocumentTypeNamesExtending(docType);
    }

    public boolean isProxySchema(String schema, String docType) {
        return delegate.isProxySchema(schema, docType);
    }

    public int getDocumentTypesCount() {
        return delegate.getDocumentTypesCount();
    }

    public boolean hasSuperType(String docType, String superType) {
        return delegate.hasSuperType(docType, superType);
    }

    public Set<String> getAllowedSubTypes(String type) {
        return delegate.getAllowedSubTypes(type);
    }

    public PropertyDeprecationHandler getDeprecatedProperties() {
        return delegate.getDeprecatedProperties();
    }

    public PropertyDeprecationHandler getRemovedProperties() {
        return delegate.getRemovedProperties();
    }

    public boolean getClearComplexPropertyBeforeSet() {
        return delegate.getClearComplexPropertyBeforeSet();
    }

    public boolean getAllowVersionWriteForDublinCore() {
        return delegate.getAllowVersionWriteForDublinCore();
    }
}
