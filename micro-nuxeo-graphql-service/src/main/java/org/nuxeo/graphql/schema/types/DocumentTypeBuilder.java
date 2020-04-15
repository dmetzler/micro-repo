package org.nuxeo.graphql.schema.types;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.core.schema.types.primitives.BooleanType;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.schema.types.primitives.DoubleType;
import org.nuxeo.ecm.core.schema.types.primitives.IntegerType;
import org.nuxeo.ecm.core.schema.types.primitives.LongType;
import org.nuxeo.ecm.core.schema.types.primitives.StringType;
import org.nuxeo.graphql.descriptors.AliasDescriptor;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.graphql.schema.fetcher.DocPropertyDataFetcher;
import org.nuxeo.graphql.schema.fetcher.DocumentPropertyDataFetcher;
import org.nuxeo.graphql.schema.fetcher.QueryDataFetcher;
import org.nuxeo.graphql.schema.fetcher.SchemaDataFetcher;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

public class DocumentTypeBuilder extends GraphQLObjectType.Builder {

    private DocumentType type;

    private NuxeoGQLSchemaManager sm;

    private DocumentTypeBuilder(DocumentType type, NuxeoGQLSchemaManager sm) {
        this.type = type;
        this.sm = sm;
    }

    @Override
    public GraphQLObjectType build() {

        Builder builder = newObject().name(type.getName()).withInterface(sm.getDocumentInterface());
        builder.field(newFieldDefinition().type(GraphQLString)//
                                          .name("_path")
                                          .dataFetcher(new DocPropertyDataFetcher())
                                          .build())
               .field(newFieldDefinition().type(GraphQLString)//
                                          .name("_id")
                                          .dataFetcher(new DocPropertyDataFetcher())
                                          .build())
               .field(newFieldDefinition().type(GraphQLString)//
                                          .name("_name")
                                          .dataFetcher(new DocPropertyDataFetcher())
                                          .build());

        for (Schema schema : type.getSchemas()) {
            String name = schema.getNamespace().prefix;
            name = StringUtils.isNotBlank(name) ? name : schema.getName();
            GraphQLObjectType typeForSchema = typeForSchema(schema.getName());

            if (!typeForSchema.getFieldDefinitions().isEmpty()) {
                builder.field(newFieldDefinition().name(name)
                                                  .type(typeForSchema)
                                                  .dataFetcher(new SchemaDataFetcher(schema))
                                                  .build());
            }
        }

        sm.getAliases().values().stream().filter(a -> a.targetDoctype.equals(type.getName())).forEach(a -> {
            builder.field(newFieldDefinition().name(a.name)
                                              .type(getTypeForAlias(a, sm))
                                              .dataFetcher(getFetcherForAlias(a))
                                              .build());
        });
        return builder.build();

    }

    private GraphQLOutputType getTypeForAlias(AliasDescriptor alias, NuxeoGQLSchemaManager sm) {
        if ("prop".equals(alias.type)) {
            return GraphQLString;
        } else if ("query".equals(alias.type)) {

            if (alias.args.size() > 1) {
                return new GraphQLList(new GraphQLTypeReference(alias.args.get(1)));
            } else {
                return new GraphQLList(new GraphQLTypeReference("document"));
            }
        } else {
            return null;
        }
    }

    private DataFetcher getFetcherForAlias(AliasDescriptor alias) {
        if ("prop".equals(alias.type)) {
            return new DocumentPropertyDataFetcher(alias.args.get(0));
        } else if ("query".equals(alias.type)) {
            QueryDataFetcher queryDataFetcher = new QueryDataFetcher(alias.args.get(0));
            return new VertxDataFetcher<>(queryDataFetcher::get);
        } else {
            return null;
        }
    }

    /**
     * Creates a GQL type for a Nuxeo schema
     *
     * @param schemaName
     * @return
     */
    private GraphQLObjectType typeForSchema(String schemaName) {
        Map<String, GraphQLObjectType> typesForSchema = sm.getTypeRegistry();

        if (!typesForSchema.containsKey(schemaName)) {
            Schema s = sm.getSchemaManager().getSchema(schemaName);

            Builder schemaBuilder = newObject().name("schema_" + schemaName);

            for (Field f : s.getFields()) {
                if (!f.getName().getLocalName().matches("[_A-Za-z][_0-9A-Za-z]*")) {
                    continue;
                }

                Type t = f.getType();
                if (t.isSimpleType()) {
                    graphql.schema.GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition().name(
                            f.getName().getLocalName())
                                                                                                     .dataFetcher(
                                                                                                             new DocumentPropertyDataFetcher(
                                                                                                                     f.getName()
                                                                                                                      .getPrefixedName()));
                    if (t instanceof StringType) {
                        fieldBuilder.type(GraphQLString);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof BooleanType) {
                        fieldBuilder.type(GraphQLBoolean);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof DateType) {
                        fieldBuilder.type(GraphQLString);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof DoubleType) {
                        fieldBuilder.type(GraphQLFloat);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof IntegerType) {
                        fieldBuilder.type(GraphQLInt);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof LongType) {
                        fieldBuilder.type(GraphQLLong);
                        schemaBuilder.field(fieldBuilder.build());
                    }

                }
            }
            typesForSchema.put(schemaName, schemaBuilder.build());
        }
        return typesForSchema.get(schemaName);
    }

    public static DocumentTypeBuilder newDocumentType(DocumentType type, NuxeoGQLSchemaManager sm) {
        return new DocumentTypeBuilder(type, sm);
    }
}
