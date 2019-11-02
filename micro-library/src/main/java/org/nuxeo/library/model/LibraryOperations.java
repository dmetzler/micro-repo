package org.nuxeo.library.model;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.vertx.graphql.Mutation;
import org.nuxeo.vertx.graphql.Query;
import org.nuxeo.vertx.graphql.Schema;

import graphql.schema.DataFetchingEnvironment;

@Schema("library.graphqls")
public class LibraryOperations {


    // allLibraries(country: String, city: String) [Library]
    @Query("allLibraries")
    public List<Library> allLibraries(DataFetchingEnvironment env, CoreSession session) {
        String country = env.getArgument("country");
        String city = env.getArgument("city");

        QueryBuilder qb = new QueryBuilder().from("Library");
        if (StringUtils.isNotBlank(country)) {
            qb.addClause(Library.LIB_COUNTRY, country);
        }

        if (StringUtils.isNotBlank(city)) {
            qb.addClause(Library.LIB_CITY, city);
        }
        String query = qb.build();

        return session.query(query).stream().map(Library::fromDoc).collect(toList());
    }

    // libraryById(id: String!): Library
    @Query("libraryById")
    public Library libraryById(DataFetchingEnvironment env, CoreSession session) {
        String id = env.getArgument("id");
        IdRef ref = new IdRef(id);
        if (session.exists(ref)) {
            return Library.fromDoc(session.getDocument(ref));
        } else {
            return null;
        }
    }

    // newLibrary(name: String!, city: String!, country: String!): Library
    @Mutation("newLibrary")
    public Library newLibrary(DataFetchingEnvironment env, CoreSession session) {
        String name = env.getArgument("name");
        String city = env.getArgument("city");
        String country = env.getArgument("country");

        Library library = new Library.Builder().name(name).city(city).country(country).build();
        DocumentModel libraryDoc = session.createDocument(library.toDoc(session));
        return Library.fromDoc(libraryDoc);

    }

    // deleteLibrary(libraryId: String!): Library
    @Mutation("deleteLibrary")
    public Library delete(DataFetchingEnvironment env, CoreSession session) {
        String id = env.getArgument("id");
        IdRef ref = new IdRef(id);
        if (session.exists(ref)) {
            Library library = Library.fromDoc(session.getDocument(ref));
            session.removeDocument(ref);
            return library;
        } else {
            return null;
        }

    }


    public static class QueryBuilder {

        private String docType = "Document";
        private List<String> clauses = new ArrayList<>();

        public void addClause(String field, String value) {
            clauses.add(String.format("%s = '%s'", field, value));
        }

        public QueryBuilder from(String docType) {
            this.docType = docType;
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder("SELECT * FROM ");
            sb.append(docType);
            if (!clauses.isEmpty()) {
                sb.append(String.join(" AND ", clauses));
            }
            return sb.toString();

        }
    }
}
