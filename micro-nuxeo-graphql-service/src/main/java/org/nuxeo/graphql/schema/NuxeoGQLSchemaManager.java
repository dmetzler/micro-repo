package org.nuxeo.graphql.schema;

import static graphql.Scalars.GraphQLString;
import static graphql.Scalars.GraphQLLong;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.graphql.descriptors.AliasDescriptor;
import org.nuxeo.graphql.descriptors.CrudDescriptor;
import org.nuxeo.graphql.descriptors.QueryDescriptor;
import org.nuxeo.graphql.schema.fetcher.DocPropertyDataFetcher;
import org.nuxeo.graphql.schema.fetcher.DocumentModelDataFetcher;
import org.nuxeo.graphql.schema.fetcher.DocumentMutationDataFetcher;
import org.nuxeo.graphql.schema.fetcher.DocumentMutationDataFetcher.Mode;
import org.nuxeo.graphql.schema.fetcher.NxqlQueryDataFetcher;
import org.nuxeo.graphql.schema.types.DocumentInputTypeBuilder;
import org.nuxeo.graphql.schema.types.DocumentTypeBuilder;
import org.nuxeo.graphql.schema.types.QueryFieldTypeBuilder;
import org.nuxeo.micro.repo.proto.Document;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.TypeResolver;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

public class NuxeoGQLSchemaManager {

    private GraphQLInterfaceType documentInterface;

    private Map<String, GraphQLObjectType> docTypeToGQLType = new HashMap<>();

    private Map<String, GraphQLObjectType> typesForSchema = new HashMap<>();

    private Map<String, GraphQLInputObjectType> inputTypesForSchema = new HashMap<>();

    private Map<String, AliasDescriptor> aliases = new HashMap<>();

    private Map<String, QueryDescriptor> queries = new HashMap<>();

    private Map<String, CrudDescriptor> cruds = new HashMap<>();

    private SchemaManager sm;

    private DocumentModelDataFetcher dmDataFetcher;

    public NuxeoGQLSchemaManager(SchemaManager sm) {
        this.sm = sm;
        dmDataFetcher = new DocumentModelDataFetcher();
    }

    public NuxeoGQLSchemaManager(Map<String, AliasDescriptor> aliases, Map<String, QueryDescriptor> queries,
            Map<String, CrudDescriptor> cruds, SchemaManager sm) {
        this.aliases = aliases;
        this.queries = queries;
        this.cruds = cruds;
        this.sm = sm;

        dmDataFetcher = new DocumentModelDataFetcher();
    }

    public GraphQLSchema getNuxeoSchema() {
        buildNuxeoTypes();
        Set<GraphQLType> dictionary = new HashSet<>(docTypeToGQLType.values());
        graphql.schema.GraphQLSchema.Builder builder = GraphQLSchema.newSchema().query(buildQueryType());
        if (cruds.size() > 0) {
            builder.mutation(buildMutationType());
        }
        return builder.build(dictionary);

    }

    public SchemaManager getSchemaManager() {
        return sm;
    }

    private GraphQLObjectType buildQueryType() {

        NxqlQueryDataFetcher nxqlDataFetcher = new NxqlQueryDataFetcher();

        Builder builder = newObject().name("nuxeo");
        builder.field(newFieldDefinition().name("document")
                                          .type(documentInterface)
                                          .argument(new GraphQLArgument("path", GraphQLString))
                                          .argument(new GraphQLArgument("id", GraphQLString))
                                          .dataFetcher(new VertxDataFetcher<>(dmDataFetcher::get))
                                          .build())
               .field(newFieldDefinition().name("documents")
                                          .type(new GraphQLList(documentInterface))
                                          .argument(new GraphQLArgument("nxql", new GraphQLNonNull(GraphQLString)))
                                          .dataFetcher(new VertxDataFetcher<>(nxqlDataFetcher::get))
                                          .build());

        for (QueryDescriptor query : queries.values()) {
            builder.field(QueryFieldTypeBuilder.newField(query, this).build());
        }

        for (CrudDescriptor crud : cruds.values()) {
            buildQueriesForDocType(builder, crud.targetDoctype);
        }

        return builder.build();

    }

    private void buildQueriesForDocType(Builder builder, String docType) {
        QueryDescriptor qd = new QueryDescriptor();
        qd.query = String.format("SELECT * FROM %s", docType);
        qd.resultType = docType;
        qd.name = String.format("all%s", plural(docType));

        // allDocType
        QueryFieldTypeBuilder allField = QueryFieldTypeBuilder.newField(qd, this);
        builder.field(allField.build());
        // _allDocTypeMeta
        builder.field(allField.buildMeta());

        builder.field(newFieldDefinition().name(docType)
                                          .type(docTypeToGQLType.get(docType))
                                          .argument(new GraphQLArgument("id", GraphQLString))
                                          .dataFetcher(new VertxDataFetcher<>(dmDataFetcher::get))
                                          .build());

    }

    public static String plural(String objectName) {
        if (objectName.endsWith("y")) {
            return String.format("%sies", objectName.substring(0, objectName.length() - 1));
        } else {
            return String.format("%ss", objectName);
        }
    }

    private Builder buildMutationType() {
        Builder builder = newObject().name("nuxeoMutations");

        for (CrudDescriptor crud : cruds.values()) {
            buildMutationForDocType(builder, crud.targetDoctype);
        }

        return builder;
    }

    private void buildMutationForDocType(Builder builder, String docType) {
        GraphQLInputObjectType inputType = DocumentInputTypeBuilder.type(docType, this).build();

        DocumentMutationDataFetcher creationFetcher = new DocumentMutationDataFetcher(docType, Mode.CREATE,this);
        DocumentMutationDataFetcher updateFetcher = new DocumentMutationDataFetcher(docType, Mode.UPDATE,this);
        DocumentMutationDataFetcher deleteFetcher = new DocumentMutationDataFetcher(docType, Mode.DELETE,this);


        builder.field(creationFetcher.buildFieldDefinition(this));
        builder.field(updateFetcher.buildFieldDefinition(this));
        builder.field(deleteFetcher.buildFieldDefinition(this));


//        builder.field(newFieldDefinition().name("create" + docType)
//                                          .type(docTypeToGQLType.get(docType))
//                                          .argument(newArgument().name(docType).type(inputType))
//                                          .argument(newArgument().name("parentPath").type(GraphQLString))
//                                          .argument(newArgument().name("name").type(GraphQLString))
//
//                                          .dataFetcher(new VertxDataFetcher<>(creationFetcher::get)));

//        builder.field(newFieldDefinition().name("update" + docType)
//                                          .type(docTypeToGQLType.get(docType))
//                                          .argument(newArgument().name("id").type(GraphQLString))
//                                          .argument(newArgument().name(docType).type(inputType))
//                                          .dataFetcher(new VertxDataFetcher<>(updateFetcher::get)));
//
//        builder.field(newFieldDefinition().name("delete" + docType)
//                                          .type(docTypeToGQLType.get(docType))
//                                          .argument(newArgument().name("id").type(GraphQLString))
//                                          .dataFetcher(new VertxDataFetcher<>(deleteFetcher::get)));
    }

    /**
     * Build a list of GraphQL types corresponding to each Nuxeo doc type.
     *
     * @return
     */
    private void buildNuxeoTypes() {

        if (documentInterface == null) {

            docTypeToGQLType = new HashMap<>();
            GraphQLObjectType listMetadata = newObject().name("ListMetadata")
                                                        .field(newFieldDefinition().name("count").type(GraphQLLong))
                                                        .build();

            docTypeToGQLType.put("__metaData", listMetadata);

            documentInterface = newInterface().name("document")
                                              .field(newFieldDefinition().type(GraphQLString)//
                                                                         .name("_path")
                                                                         .dataFetcher(new DocPropertyDataFetcher())
                                                                         .build())
                                              .field(newFieldDefinition().type(GraphQLString)//
                                                                         .name("_id")
                                                                         .dataFetcher(new DocPropertyDataFetcher())
                                                                         .build())
                                              .field(newFieldDefinition().type(GraphQLString)//
                                                                         .name("id")
                                                                         .dataFetcher(new DocPropertyDataFetcher())
                                                                         .build())
                                              .field(newFieldDefinition().type(GraphQLString)//
                                                                         .name("_name")
                                                                         .dataFetcher(new DocPropertyDataFetcher())
                                                                         .build())
                                              .typeResolver(getNuxeoDocumentTypeResolver())
                                              .build();

            for (DocumentType type : getSchemaManager().getDocumentTypes()) {
                docTypeToGQLType.put(type.getName(), DocumentTypeBuilder.newDocumentType(type, this).build());
            }
        }

    }

    /**
     * Creates a GQL type for a Nuxeo schema
     *
     * @param schemaName
     * @return
     */

    private TypeResolver getNuxeoDocumentTypeResolver() {
        return new TypeResolver() {

            @Override
            public GraphQLObjectType getType(TypeResolutionEnvironment tre) {
                if (tre.getObject() instanceof Document) {
                    return docTypeToGQLType.get(((Document) tre.getObject()).getType());
                } else {
                    return null;
                }
            }
        };
    }

    public GraphQLInterfaceType getDocumentInterface() {
        return documentInterface;
    }

    public GraphQLObjectType docTypeToGQLType(String resultType) {
        return docTypeToGQLType.get(resultType);

    }

    public Map<String, GraphQLInputObjectType> getInputTypeRegistry() {
        return inputTypesForSchema;
    }

    public Map<String, GraphQLObjectType> getTypeRegistry() {
        return typesForSchema;
    }

    public Map<String, AliasDescriptor> getAliases() {
        return aliases;
    }

    public GraphQLObjectType getListMetadataType() {
        return docTypeToGQLType.get("__metaData");
    }

}
