package org.nuxeo.micro.repo.service.graphql.tenant;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    private String docType = "Document";
    private List<String> clauses = new ArrayList<>();
    private String select = "*";

    public void addClause(String field, String value) {
        clauses.add(String.format("%s = '%s'", field, value));
    }

    public QueryBuilder count(String doctype) {
        this.docType = doctype;
        return this;
    }

    public QueryBuilder from(String docType) {
        this.docType = docType;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(select);
        sb.append(" FROM ");
        sb.append(docType);
        if (!clauses.isEmpty()) {
            sb.append(" WHERE ");
            sb.append(String.join(" AND ", clauses));
        }
        return sb.toString();

    }
}