package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.micro.repo.proto.Document;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class DocPropertyDataFetcher implements DataFetcher<Object> {

    @Override
    public Object get(DataFetchingEnvironment environment) {
        String fieldName = getFieldName(environment);
        if (environment.getSource() instanceof Document) {
            Document doc = (Document) environment.getSource();
            if ("_path".equals(fieldName)) {
                return doc.getParentPath() + "/" + doc.getName();
            } else if ("_id".equals(fieldName)) {
                return doc.getUuid();
            }
            else if ("_name".equals(fieldName)) {
                return doc.getName();
            }
        }
        return null;
    }

    private String getFieldName(DataFetchingEnvironment environment) {
        String fieldName = environment.getFields().get(0).getName();
        return fieldName;
    }
}
