package org.nuxeo.library.model;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public class Library {

    static final String LIB_COUNTRY = "lib:country";
    static final String LIB_CITY = "lib:city";

    protected String id;
    protected String name;
    protected String path;
    protected String city;
    protected String country;
    protected String creator;

    public Library(DocumentModel doc) {
        this.id = doc.getId();
        this.name = doc.getName();
        this.path = doc.getPathAsString();
        this.city = doc.getProperty(LIB_CITY).getValue(String.class);
        this.country = doc.getProperty(LIB_COUNTRY).getValue(String.class);
        this.creator = doc.getProperty("dc:creator").getValue(String.class);

    }

    private Library() {

    }

    public DocumentModel toDoc(CoreSession session) {
        DocumentModel doc = session.createDocumentModel("/", name, "Library");
        return toDoc(doc);

    }

    public DocumentModel toDoc(DocumentModel doc) {
        doc.setPropertyValue(LIB_CITY, city);
        doc.setPropertyValue(LIB_COUNTRY, country);
        return doc;
    }

    public static Library fromDoc(DocumentModel doc) {
        return new Library(doc);
    }

    public static class Builder {
        private String name;
        private String city;
        private String country;

        public Library build() {
            Library result = new Library();
            result.name = this.name;
            result.city = this.city;
            result.country = this.country;
            return result;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }
    }

}
