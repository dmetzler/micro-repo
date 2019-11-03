package org.nuxeo.library.model;

import java.io.Serializable;
import java.util.Map;

public class LibraryFilter {

    public String q;
    public String id;
    public String city;
    public String country;

    public LibraryFilter(Map<String, Serializable> filter) {
        if (filter != null) {
            this.q = (String) filter.get("q");
            this.id = (String) filter.get("id");
            this.city = (String) filter.get("city");
            this.country = (String) filter.get("country");
        }
    }
}
