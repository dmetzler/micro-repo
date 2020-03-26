package org.nuxeo.micro.repo.proto.utils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.SimpleDocumentModel;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.api.model.ReadOnlyPropertyException;
import org.nuxeo.ecm.core.api.model.impl.ArrayProperty;
import org.nuxeo.ecm.core.api.model.impl.DocumentPartImpl;
import org.nuxeo.ecm.core.api.model.impl.PropertyFactory;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.SimpleType;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.core.schema.types.primitives.BinaryType;
import org.nuxeo.ecm.core.schema.types.primitives.BooleanType;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.schema.types.primitives.DoubleType;
import org.nuxeo.ecm.core.schema.types.primitives.IntegerType;
import org.nuxeo.ecm.core.schema.types.primitives.LongType;
import org.nuxeo.ecm.core.schema.types.primitives.StringType;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.Document.Builder;
import org.nuxeo.micro.repo.proto.Document.Property.PropType;
import org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty;

public class DocumentModelMapper {

    public Document toDocument(DocumentModel doc, SchemaManager sm) {
        Builder builder = Document.newBuilder();

        builder.setRepositoryName(doc.getRepositoryName())//
                .setUuid(doc.getId())//
                .setName(doc.getName())//
                .setParentPath(
                        doc.getPathAsString().substring(0, doc.getPathAsString().length() - doc.getName().length() - 1))
                .setType(doc.getType())//
                .addAllFacets(doc.getFacets())//
                .addAllSchema(Arrays.asList(doc.getSchemas()).stream()
                        .map(s -> Document.Schema.newBuilder().setPrefix(s).build()).collect(Collectors.toList()))//
                .setParentRef(doc.getParentRef().toString())//
                .setChangeToken(doc.getChangeToken())//
                .setTitle(doc.getTitle())
                // .setLastModified()
                .setVersionLabel(doc.getVersionLabel())//
                .setIsCheckedOut(doc.isCheckedOut());

        if (doc.getCurrentLifeCycleState() != null) {
            builder.setState(doc.getCurrentLifeCycleState());
        }

//        if (doc.getLockInfo() != null) {
//            builder.setLockOwner(doc.getLockInfo().getOwner())
//                    .setLockCreated(doc.getLockInfo().getCreated().getTimeInMillis());//
//        }

        String[] docSchemas = doc.getSchemas();
        for (String schemaName : docSchemas) {
            Schema schema = sm.getSchema(schemaName);
            if (schema != null) {
                String prefix = schema.getNamespace().prefix;
                if (prefix == null || prefix.length() == 0) {
                    prefix = schemaName;
                }
                prefix = prefix + ":";
                builder.addSchema(Document.Schema.newBuilder().setName(schemaName).setPrefix(prefix).build());

                for (Field field : schema.getFields()) {
                    String prefixedName = prefix + field.getName().getLocalName();
                    Property property = doc.getProperty(prefixedName);

                    org.nuxeo.micro.repo.proto.Document.Property prop = buildProperty(property);
                    if (prop != null) {
                        builder.putProperties(prefixedName, prop);
                    }

                }
            }
        }

        return builder.build();
    }

    static org.nuxeo.micro.repo.proto.Document.Property buildProperty(Property prop) {
        Document.Property.Builder propBuilder = Document.Property.newBuilder();
        if (prop.isScalar()) {
            Type type = prop.getType();
            Serializable value = prop.getValue();

            org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty.Builder scalarBuilder = Document.Property.ScalarProperty
                    .newBuilder();

            if (value != null) {

                if (type instanceof BooleanType) {
                    scalarBuilder.setBooleanValue((boolean) value);
                    propBuilder.setType(PropType.BOOL);
                } else if (type instanceof LongType) {
                    scalarBuilder.setIntValue(((Number) value).longValue()); // value may be a DeltaLong
                    propBuilder.setType(PropType.INT);
                } else if (type instanceof DoubleType) {
                    scalarBuilder.setDoubleValue((Double) value);
                    propBuilder.setType(PropType.DOUBLE);
                } else if (type instanceof IntegerType) {
                    scalarBuilder.setIntValue((Integer) value);
                    propBuilder.setType(PropType.INT);
                } else if (type instanceof BinaryType) {
                    throw new UnsupportedOperationException("byte type is not yet supported");
                } else if (type instanceof DateType) {
                    if (value instanceof Date) {
                        scalarBuilder.setIntValue(((Date) value).getTime());
                    } else if (value instanceof Calendar) {
                        scalarBuilder.setIntValue(((Calendar) value).getTimeInMillis());
                    }
                    propBuilder.setType(PropType.DATE);

                } else {
                    propBuilder.setType(PropType.STRING);
                    scalarBuilder.setStrValue(type.encode(value));
                }

                propBuilder.setMultiple(false);
                propBuilder.addScalarValue(scalarBuilder.build());
                return propBuilder.build();
            }
        }
        return null;

//        else if (prop.isList()) {
////            writeListProperty(jg, prop);
////        } else if (prop instanceof BlobProperty) { // a blob
////            writeBlobProperty(jg, (BlobProperty) prop);
//        } else if (prop.isComplex()) {
////            writeComplexProperty(jg, prop);
//        } else if (prop.isPhantom()) {
////            jg.writeNull();
//        }
    }

    public DocumentModel toDocumentModel(Document protoDoc, CoreSession coreSession) {

        DocumentModel doc = initDocument(protoDoc, coreSession);

        SchemaManager sm = coreSession.getRepository().getSchemaManager();

        for (Entry<String, Document.Property> prop : protoDoc.getPropertiesMap().entrySet()) {
            String prefix = prop.getKey().split(":")[0];
            Schema schema = sm.getSchemaFromPrefix(prefix);

            DocumentPart part = doc.getPart(schema.getName());
            Property propValue = toProp(sm, part, prop.getKey(), prop.getValue());
            if (propValue != null) {
                doc.setPropertyValue(prop.getKey(), propValue.getValue());
            }
        }

        return doc;
    }

    private Property toProp(SchemaManager schemaManager, DocumentPart parent, String propertyName,
            org.nuxeo.micro.repo.proto.Document.Property prop) {

        Field field = null;
        if (propertyName.contains(":")) {
            field = schemaManager.getField(propertyName);
            if (field == null) {
                return null;
            }
        }
        if (field == null) {
            return null;
        }
        return readProperty(parent, field, prop);

    }

    private Property readProperty(DocumentPart parent, Field field,
            org.nuxeo.micro.repo.proto.Document.Property prop) {
        Property property = PropertyFactory.createProperty(parent, field, 0);
        property.setForceDirty(true);
        if (property.isScalar()) {
            fillScalarProperty(property, prop);
        }
//        else if (property.isList()) {
//            fillListProperty(property, jn);
//        } else {
//            if (!(property instanceof BlobProperty)) {
//                fillComplexProperty(property, jn);
//            } else {
//                Blob blob = readEntity(Blob.class, Blob.class, jn);
//                property.setValue(blob);
//            }
//        }
        property.setForceDirty(false);
        return property;
    }

    private void fillScalarProperty(Property property, org.nuxeo.micro.repo.proto.Document.Property prop) {
        // TODO Auto-generated method stub
        if ((property instanceof ArrayProperty) && prop.getMultiple()) {
            Type fieldType = ((ListType) property.getType()).getFieldType();

            List<Object> values = prop.getScalarValueList().stream()
                    .map(s -> getScalarPropertyValue(property, s, fieldType)).collect(Collectors.toList());

            property.setValue(castArrayPropertyValue(((SimpleType) fieldType).getPrimitiveType(), values));
        } else {
            property.setValue(getScalarPropertyValue(property, prop.getScalarValue(0), property.getType()));
        }

    }

    @SuppressWarnings({ "unchecked" })
    private <T> T[] castArrayPropertyValue(Type type, List<Object> values) {
        if (type instanceof StringType) {
            return values.toArray((T[]) Array.newInstance(String.class, values.size()));
        } else if (type instanceof BooleanType) {
            return values.toArray((T[]) Array.newInstance(Boolean.class, values.size()));
        } else if (type instanceof LongType) {
            return values.toArray((T[]) Array.newInstance(Long.class, values.size()));
        } else if (type instanceof DoubleType) {
            return values.toArray((T[]) Array.newInstance(Double.class, values.size()));
        } else if (type instanceof IntegerType) {
            return values.toArray((T[]) Array.newInstance(Integer.class, values.size()));
        } else if (type instanceof BinaryType) {
            return values.toArray((T[]) Array.newInstance(Byte.class, values.size()));
        } else if (type instanceof DateType) {
            return values.toArray((T[]) Array.newInstance(Calendar.class, values.size()));
        }
        return null;
    }

    private Object getScalarPropertyValue(Property property,
            org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty prop, Type type) {

        return getPropertyValue(((SimpleType) type).getPrimitiveType(), prop);

    }

    private Object getPropertyValue(Type type, org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty prop) {
        Object value;
        if (type instanceof BooleanType) {
            value = prop.getBooleanValue();
        } else if (type instanceof LongType) {
            value = prop.getIntValue();
        } else if (type instanceof DoubleType) {
            value = prop.getDoubleValue();
        } else if (type instanceof IntegerType) {
            value = prop.getIntValue();
        } else if (type instanceof BinaryType) {
            value = null;
        } else {
            value = type.decode(prop.getStrValue());
        }
        return value;
    }

    public DocumentModel initDocument(Document protoDoc, CoreSession coreSession) {
        String uid = protoDoc.getUuid();
        if (StringUtils.isNotBlank(uid)) {
            DocumentModel doc = coreSession.getDocument(new IdRef(uid));
            String changeToken = protoDoc.getChangeToken();
            doc.putContextData(CoreSession.CHANGE_TOKEN, changeToken);
            return doc;
        } else {
            String type = protoDoc.getType();
            SchemaManager schemaManager = coreSession.getRepository().getSchemaManager();
            DocumentModel doc = StringUtils.isNotBlank(type) ? SimpleDocumentModel.ofType(type, schemaManager)
                    : SimpleDocumentModel.empty(schemaManager);
            String name = protoDoc.getName();
            if (StringUtils.isNotBlank(name)) {
                doc.setPathInfo(null, name);
            }
            return doc;
        }
    }

    public static void applyPropertyValues(DocumentModel src, DocumentModel dst, SchemaManager sm) {
        applyPropertyValues(src, dst, true, sm);
        // copy change token
        dst.getContextData().putAll(src.getContextData());
    }

    public static void applyPropertyValues(DocumentModel src, DocumentModel dst, boolean dirtyOnly, SchemaManager sm) {
        // if not "dirty only", it handles all the schemas for the given type
        // so it will trigger the default values initialization
        if (dirtyOnly) {
            applyDirtyPropertyValues(src, dst);
        } else {
            applyAllPropertyValues(src, dst, sm);
        }
    }

    public static void applyDirtyPropertyValues(DocumentModel src, DocumentModel dst) {
        Stream.of(src.getSchemas()).flatMap(s -> src.getPropertyObjects(s).stream()).filter(Property::isDirty)
                .forEach(p -> applyPropertyValue(p, dst));
    }

    public static void applyAllPropertyValues(DocumentModel src, DocumentModel dst, SchemaManager sm) {
        DocumentType type = sm.getDocumentType(src.getType());
        Stream.of(type.getSchemaNames()).flatMap(s -> src.getPropertyObjects(s).stream())
                .forEach(p -> applyPropertyValue(p, dst));
    }

    protected static void applyPropertyValue(Property property, DocumentModel dst) {
        try {
            dst.setPropertyValue(getXPath(property), property.getValue());
        } catch (PropertyNotFoundException | ReadOnlyPropertyException e) {
            // log.trace("Can't apply property: {} to dst: {}", property, dst, e);
        }
    }

    protected static String getXPath(Property property) {
        String xpath = property.getXPath();
        // if no prefix, use schema name as prefix
        if (!xpath.contains(":")) {
            xpath = property.getSchema().getName() + ":" + xpath;
        }
        return xpath;
    }
}
