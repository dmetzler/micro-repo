package org.nuxeo.library.model;

import java.io.Serializable;
import java.util.Map;

public class LibraryFilter {

    public String q;
    public String id;
    public String city;
    public String country;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((q == null) ? 0 : q.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LibraryFilter other = (LibraryFilter) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (q == null) {
            if (other.q != null)
                return false;
        } else if (!q.equals(other.q))
            return false;
        return true;
    }

    public LibraryFilter(Map<String, Serializable> filter) {
        if (filter != null) {
            this.q = (String) filter.get("q");
            this.id = (String) filter.get("id");
            this.city = (String) filter.get("city");
            this.country = (String) filter.get("country");
        }
    }
}
