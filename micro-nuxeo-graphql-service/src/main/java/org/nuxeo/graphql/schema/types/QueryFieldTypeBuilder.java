package org.nuxeo.graphql.schema.types;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.nuxeo.graphql.descriptors.QueryDescriptor;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.graphql.schema.fetcher.QueryDataFetcher;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldDefinition.Builder;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;

public class QueryFieldTypeBuilder extends Builder {

    private QueryDescriptor query;
    private NuxeoGQLSchemaManager sm;

    private QueryFieldTypeBuilder(QueryDescriptor query, NuxeoGQLSchemaManager sm) {
        this.query = query;
        this.sm = sm;
    }

    @Override
    public GraphQLFieldDefinition build() {

        Builder fieldBuilder = newFieldDefinition().name(query.name);
        if (query.args.size() > 0) {
            for (String arg : query.args) {
                fieldBuilder.argument(new GraphQLArgument(arg, new GraphQLNonNull(GraphQLString)));
            }
        }

        if ("document".equals(query.resultType)) {
            fieldBuilder.type(new GraphQLList(sm.getDocumentInterface()));
        } else {
            fieldBuilder.type(new GraphQLList(sm.docTypeToGQLType(query.resultType)));
        }

        fieldBuilder.dataFetcher(new QueryDataFetcher(query.query));
        return fieldBuilder.build();

    }

    public static QueryFieldTypeBuilder newField(QueryDescriptor query, NuxeoGQLSchemaManager sm) {
        return new QueryFieldTypeBuilder(query, sm);
    }
}
