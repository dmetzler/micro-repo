package org.nuxeo.micro.repo.proto.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.nuxeo.micro.repo.proto.Document.Property;
import org.nuxeo.micro.repo.proto.Document.Property.PropType;

public class DocumentBuilder {

    private String type;

    private String id;

    Map<String, Property> props = new HashMap<>();

    public static DocumentBuilder create(String type) {
        return new DocumentBuilder().newDocument(type);
    }

    public DocumentBuilder newDocument(String type) {
        this.type = type;
        return this;
    }

    public Builder toBuilder() {
        Builder builder = Document.newBuilder().setType(type).putAllProperties(props);
        if(StringUtils.isNotBlank(id)) {
            builder.setUuid(id);
        }
        return builder;
    }

    public DocumentBuilder setPropertyValue(String xPath, Serializable value) {
        return setPropertyValue(xPath, value, StringType.INSTANCE);
    }

    public DocumentBuilder setPropertyValue(String xPath, Serializable value, Type propType) {

        org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty.Builder scalarBuilder = Document.Property.ScalarProperty
                .newBuilder();

        org.nuxeo.micro.repo.proto.Document.Property.Builder propBuilder = Document.Property.newBuilder();

        if (propType instanceof BooleanType) {
            scalarBuilder.setBooleanValue((boolean) value);
            propBuilder.setType(PropType.BOOL);
        } else if (propType instanceof LongType) {
            scalarBuilder.setIntValue(((Number) value).longValue()); // value may be a DeltaLong
            propBuilder.setType(PropType.INT);
        } else if (propType instanceof DoubleType) {
            scalarBuilder.setDoubleValue((Double) value);
            propBuilder.setType(PropType.DOUBLE);
        } else if (propType instanceof IntegerType) {
            scalarBuilder.setIntValue((Integer) value);
            propBuilder.setType(PropType.INT);
        } else if (propType instanceof BinaryType) {
            throw new UnsupportedOperationException("byte type is not yet supported");
        } else if (propType instanceof DateType) {
            if (value instanceof Date) {
                scalarBuilder.setIntValue(((Date) value).getTime());
            } else if (value instanceof Calendar) {
                scalarBuilder.setIntValue(((Calendar) value).getTimeInMillis());
            }
            propBuilder.setType(PropType.DATE);

        } else {
            propBuilder.setType(PropType.STRING);
            scalarBuilder.setStrValue(propType.encode(value));
        }

        propBuilder.addScalarValue(scalarBuilder.build());
        props.put(xPath, propBuilder.build());
        return this;

    }

    public Document build() {
        return toBuilder().build();
    }

    public void setId(String id) {
        this.id = id;
    }
}
