package org.nuxeo.graphql.schema.types;

import static graphql.Scalars.GraphQLString;
import static graphql.Scalars.GraphQLInt;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.nuxeo.graphql.descriptors.QueryDescriptor;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.graphql.schema.fetcher.QueryDataFetcher;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldDefinition.Builder;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;

public class QueryFieldTypeBuilder extends Builder {

    private QueryDescriptor query;

    private NuxeoGQLSchemaManager sm;

    private QueryDataFetcher df;


    private QueryFieldTypeBuilder(QueryDescriptor query, NuxeoGQLSchemaManager sm) {
        this.query = query;
        this.sm = sm;
        df = new QueryDataFetcher(query.query);
    }


    @Override
    public GraphQLFieldDefinition build() {

        Builder fieldBuilder = newFieldDefinition().name(query.name);

        if (!query.args.isEmpty()) {
            for (String arg : query.args) {
                fieldBuilder.argument(newArgument().name(arg).type(new GraphQLNonNull(GraphQLString)));
            }
        }


        fieldBuilder.argument(newArgument().name("page").type(GraphQLInt));
        fieldBuilder.argument(newArgument().name("perPage").type(GraphQLInt));
        fieldBuilder.argument(newArgument().name("sortField").type(GraphQLString));
        fieldBuilder.argument(newArgument().name("filter").type(GraphQLString));

        if ("document".equals(query.resultType)) {
            fieldBuilder.type(new GraphQLList(sm.getDocumentInterface()));
        } else {
            fieldBuilder.type(new GraphQLList(sm.docTypeToGQLType(query.resultType)));
        }


        fieldBuilder.dataFetcher(new VertxDataFetcher<>(df::get));
        return fieldBuilder.build();

    }


    public GraphQLFieldDefinition buildMeta() {
        Builder fieldBuilder = newFieldDefinition().name(String.format("_%sMeta",query.name));

        if (query.args.size() > 0) {
            for (String arg : query.args) {
                fieldBuilder.argument(new GraphQLArgument(arg, new GraphQLNonNull(GraphQLString)));
            }
        }

        fieldBuilder.argument(newArgument().name("page").type(GraphQLInt));
        fieldBuilder.argument(newArgument().name("perPage").type(GraphQLInt));
        fieldBuilder.argument(newArgument().name("sortField").type(GraphQLString));
        fieldBuilder.argument(newArgument().name("filter").type(GraphQLString));



        fieldBuilder.type(sm.getListMetadataType());




        fieldBuilder.dataFetcher(new VertxDataFetcher<>(df::getMeta));
        return fieldBuilder.build();

    }

    public static QueryFieldTypeBuilder newField(QueryDescriptor query, NuxeoGQLSchemaManager sm) {
        return new QueryFieldTypeBuilder(query, sm);
    }

}
