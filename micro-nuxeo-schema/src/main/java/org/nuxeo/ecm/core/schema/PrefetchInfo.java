/*
 * (C) Copyright 2006-2018 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Bogdan Stefanescu
 *     Florent Guillaume
 */

package org.nuxeo.ecm.core.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.schema.types.ComplexType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information about what's to be prefetched: individual properties and whole
 * schemas.
 */
public class PrefetchInfo implements Serializable {

    private static final long serialVersionUID = -6495547095819614741L;

    private static final Logger log = LoggerFactory.getLogger(PrefetchInfo.class);

    private final String expr;

    private transient String[] fields;

    private transient String[] schemas;

    public PrefetchInfo(String expr) {
        this.expr = expr;
    }

    public String[] getSchemas(SchemaManager schemaManager) {
        parseExpression(schemaManager);
        return schemas;
    }

    public String[] getFields(SchemaManager schemaManager) {
        parseExpression(schemaManager);
        return fields;
    }

    private static final Pattern STAR_OR_DIGITS = Pattern.compile("\\*|(\\d+)");

    private void parseExpression(SchemaManager schemaManager) {
        if (fields != null || expr == null) {
            return;
        }
        List<String> fields = new ArrayList<>();
        List<String> schemas = new ArrayList<>();

        for (String s : expr.split("[ \t\n\r,]")) {
            if (s.isEmpty()) {
                continue;
            }
            s = canonicalXPath(s);

            // maybe a schema?
            Schema schema = schemaManager.getSchema(s);
            if (schema != null) {
                schemas.add(s);
                continue;
            }

            // isolate first field of property if complex
            String[] props = s.split("/");
            if (props.length == 0) {
                continue;
            }
            String prop = props[0];
            List<String> complex = Arrays.asList(props).subList(1, props.length);

            // get the field
            List<String> parts = new LinkedList<>();
            Field field;
            int i = prop.indexOf('.');
            if (i != -1) {
                // try schemaName.fieldName
                String schemaName = prop.substring(0, i);
                String fieldName = prop.substring(i + 1);
                schema = schemaManager.getSchema(schemaName);
                field = schema == null ? null : schema.getField(fieldName);
            } else {
                // otherwise try prefixed name
                field = schemaManager.getField(prop);
                // TODO must deal with prefix-less props like "size"
            }
            if (field == null) {
                logNotFound(s);
                continue;
            }
            parts.add(field.getName().getPrefixedName());

            // got a field, check its complex properties
            for (String t : complex) {
                Type fieldType = field.getType();
                if (fieldType.isComplexType()) {
                    // complex type, get subfield
                    ComplexType fieldComplexType = (ComplexType) fieldType;
                    field = fieldComplexType.getField(t);
                    parts.add(t);
                    continue;
                } else if (fieldType.isListType()) {
                    ListType listType = (ListType) fieldType;
                    if (!listType.getFieldType().isSimpleType()) {
                        // complex list
                        // should be * or an integer
                        if (!STAR_OR_DIGITS.matcher(t).matches()) {
                            field = null;
                            break;
                        }
                        field = listType.getField();
                        parts.add("*");
                    } else {
                        // array, cannot have subproperties
                        field = null;
                        break;
                    }
                } else {
                    // primitive type, cannot have subproperties
                    field = null;
                    break;
                }
            }
            if (field == null) {
                logNotFound(s);
                continue;
            }
            if (!isScalarField(field)) {
                log.error("Prefetch field '" + s + "' is not scalar");
                continue;
            }
            fields.add(StringUtils.join(parts, '/'));
        }

        this.fields = fields.toArray(new String[fields.size()]);
        this.schemas = schemas.toArray(new String[schemas.size()]);
    }

    /**
     * Checks if a field is a primitive type or array.
     */
    private static boolean isScalarField(Field field) {
        Type fieldType = field.getType();
        if (fieldType.isComplexType()) {
            // complex type
            return false;
        }
        if (!fieldType.isListType()) {
            // primitive type
            return true;
        }
        // array or complex list?
        return ((ListType) fieldType).getFieldType().isSimpleType();
    }

    private static void logNotFound(String s) {
        log.error("Prefetch field or schema '" + s + "' not found");
    }

    /**
     * Canonicalizes a Nuxeo-xpath.
     * <p>
     * Replaces {@code a/foo[123]/b} with {@code a/123/b}
     * <p>
     * A star can be used instead of the digits as well (for configuration).
     *
     * @param xpath the xpath
     * @return the canonicalized xpath.
     */
    public static String canonicalXPath(String xpath) {
        while (xpath.length() > 0 && xpath.charAt(0) == '/') {
            xpath = xpath.substring(1);
        }
        if (xpath.indexOf('[') == -1) {
            return xpath;
        } else {
            return xpath.replaceAll("[^/\\[\\]]+\\[(\\d+|\\*)\\]", "$1");
        }
    }
}
