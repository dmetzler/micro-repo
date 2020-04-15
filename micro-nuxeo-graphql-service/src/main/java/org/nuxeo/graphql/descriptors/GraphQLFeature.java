package org.nuxeo.graphql.descriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DslFeature;

public class GraphQLFeature implements DslFeature {

    private Map<String, AliasDescriptor> aliases = new HashMap<>();

    private Map<String, QueryDescriptor> queries = new HashMap<>();

    private Map<String, CrudDescriptor> cruds = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DslModel model, Map<String, Object> ast) {

        if (ast.get("aliases") != null) {
            List<Map<String, Object>> aliasList = (List<Map<String, Object>>) ast.get("aliases");

            aliasList.forEach(aliasMap -> {
                AliasDescriptor alias = new AliasDescriptor();
                alias.name = (String) aliasMap.get("name");
                alias.targetDoctype = (String) aliasMap.get("targetDoctype");
                alias.type = (String) aliasMap.get("type");
                alias.args = (List<String>) aliasMap.get("args");
                aliases.put(alias.name, alias);
            });
        }

        if (ast.get("queries") != null) {
            List<Map<String, Object>> queryList = (List<Map<String, Object>>) ast.get("queries");

            queryList.forEach(queryMap -> {
                QueryDescriptor query = new QueryDescriptor();
                query.name = (String) queryMap.get("name");
                query.query = (String) queryMap.get("query");
                query.resultType = (String) queryMap.get("resultType");
                query.args = (List<String>) queryMap.get("args");
                queries.put(query.name, query);
            });

        }

        if (ast.get("cruds") != null) {
            List<String> crudList = (List<String>) ast.get("cruds");
            crudList.forEach(c -> cruds.put(c, new CrudDescriptor(c)));
        }
    }

    public Map<String, AliasDescriptor> getAliases() {
        return aliases;
    }

    public Map<String, QueryDescriptor> getQueries() {
        return queries;
    }

    public Map<String, CrudDescriptor> getCruds() {
        return cruds;
    }

}
