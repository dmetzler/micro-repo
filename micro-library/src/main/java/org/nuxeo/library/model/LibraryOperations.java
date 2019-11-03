package org.nuxeo.library.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.vertx.graphql.ListMetadata;
import org.nuxeo.vertx.graphql.Mutation;
import org.nuxeo.vertx.graphql.Query;
import org.nuxeo.vertx.graphql.QueryBuilder;
import org.nuxeo.vertx.graphql.Schema;

import graphql.schema.DataFetchingEnvironment;

@Schema("library.graphqls")
public class LibraryOperations {

    // allLibraries(country: String, city: String) [Library]
    @Query("allLibraries")
    public List<Library> allLibraries(DataFetchingEnvironment env, CoreSession session) {

        QueryBuilder qb = new QueryBuilder().from("Library");
        LibraryFilter filter = new LibraryFilter(env.getArgument("filter"));
        fillQueryWithFilter(qb, filter);
        String query = qb.build();

        return session.query(query).stream().map(Library::fromDoc).collect(toList());
    }

    private void fillQueryWithFilter(QueryBuilder qb, LibraryFilter filter) {

        if (StringUtils.isNotBlank(filter.country)) {
            qb.addClause(Library.LIB_COUNTRY, filter.country);
        }

        if (StringUtils.isNotBlank(filter.city)) {
            qb.addClause(Library.LIB_CITY, filter.city);
        }
    }

    // _allLibrariesMeta(page: Int, perPage: Int, sortField: String, sortOrder:
    // String, filter: PostFilter): ListMetadata
    @Query("_allLibrariesMeta")
    public ListMetadata allLibrariesMeta(DataFetchingEnvironment env, CoreSession session) {
        QueryBuilder qb = new QueryBuilder().count("Library");
        LibraryFilter filter = new LibraryFilter(env.getArgument("filter"));
        fillQueryWithFilter(qb, filter);
        String query = qb.build();

        Long count = (long) session.query(query).size();
        return new ListMetadata(count);
    }

    // Library(id: ID!): Library
    @Query("Library")
    public Library libraryById(DataFetchingEnvironment env, CoreSession session) {
        String id = env.getArgument("id");
        IdRef ref = new IdRef(id);
        if (session.exists(ref)) {
            return Library.fromDoc(session.getDocument(ref));
        } else {
            return null;
        }
    }

    // createLibrary(name: String!, city: String!, country: String!): Library
    @Mutation("createLibrary")
    public Library newLibrary(DataFetchingEnvironment env, CoreSession session) {
        String name = env.getArgument("name");
        String city = env.getArgument("city");
        String country = env.getArgument("country");

        Library library = new Library.Builder().name(name).city(city).country(country).build();
        DocumentModel libraryDoc = session.createDocument(library.toDoc(session));
        return Library.fromDoc(libraryDoc);

    }

    // createLibrary(name: String!, city: String!, country: String!): Library
    @Mutation("updateLibrary")
    public Library updateLibrary(DataFetchingEnvironment env, CoreSession session) {
        String id = env.getArgument("id");
        String city = env.getArgument("city");
        String country = env.getArgument("country");

        IdRef ref = new IdRef(id);
        if (session.exists(ref)) {
            DocumentModel doc = session.getDocument(ref);
            Library library = Library.fromDoc(doc);
            library.city = city;
            library.country = country;
            doc = session.saveDocument(library.toDoc(doc));
            return Library.fromDoc(doc);
        } else {
            return null;
        }

    }

    // deleteLibrary(libraryId: ID!): Library
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
}
