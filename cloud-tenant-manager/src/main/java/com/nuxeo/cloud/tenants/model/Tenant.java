package com.nuxeo.cloud.tenants.model;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public class Tenant {

    public String id;
    public String name;
    public String customerId;
    public String environmentType;
    public boolean ready;

    public Tenant(DocumentModel doc) {
        this.id = doc.getId();
        this.name = doc.getName();
        this.customerId = doc.getProperty("tn:customerId").getValue(String.class);
        this.environmentType = doc.getProperty("tn:envType").getValue(String.class);
        this.ready = doc.getProperty("tn:ready").getValue(Boolean.class);

    }

    public Tenant(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
        this.environmentType = "dev";
        this.ready = false;
    }

    public DocumentModel toDoc(CoreSession session) {
        DocumentModel doc = session.createDocumentModel("/" + customerId, name, "Tenant");
        doc.setPropertyValue("tn:customerId", customerId);
        doc.setPropertyValue("tn:envType", "dev");
        doc.setPropertyValue("tn:ready", ready);
        return doc;

    }

    public static Tenant fromDoc(DocumentModel doc) {
        return new Tenant(doc);
    }

}
