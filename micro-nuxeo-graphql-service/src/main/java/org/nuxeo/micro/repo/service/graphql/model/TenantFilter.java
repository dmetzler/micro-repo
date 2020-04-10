package org.nuxeo.micro.repo.service.graphql.model;

import java.io.Serializable;
import java.util.Map;

public class TenantFilter {

    private String q;
    private String id;
    private String city;
    private String country;

    public TenantFilter(Map<String, Serializable> filter) {
        if (filter != null) {
            this.q = (String) filter.get("q");
            this.id = (String) filter.get("id");
            this.city = (String) filter.get("city");
            this.country = (String) filter.get("country");
        }
    }
}
