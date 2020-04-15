package org.nuxeo.graphql.schema.fetcher;

import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.Document.Property;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class DocumentPropertyDataFetcher implements DataFetcher<Object> {

    private String property;

    public DocumentPropertyDataFetcher(String property) {
        this.property = property;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object source = environment.getSource();
        if (source instanceof DataModel) {
            DataModel dm = (DataModel) source;
            return dm.getValue(property);
        } else if (source instanceof Document) {
            Document doc = (Document) source;

            Property prop = doc.getPropertiesMap().get(property);

            return prop != null ? prop.getScalarValue(0).getStrValue() : null;
        }
        return null;
    }

}
