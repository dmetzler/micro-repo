package com.nuxeo.cloud.tenants.model;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public class Tenant {

    public String id;
    public String customerId;
    public String environmentType;
    public boolean ready;

    public Tenant(DocumentModel doc) {
        this.id = doc.getName();
        this.customerId = doc.getProperty("tn:customerId").getValue(String.class);
        this.environmentType = doc.getProperty("tn:envType").getValue(String.class);
        this.ready = doc.getProperty("tn:ready").getValue(Boolean.class);

    }

    public Tenant(String customerId, String id) {
        this.customerId = customerId;
        this.id = id;
        this.environmentType = "dev";
        this.ready = false;
    }

    public DocumentModel toDoc(CoreSession session) {
        DocumentModel doc = session.createDocumentModel("/" + customerId, id, "Tenant");
        doc.setPropertyValue("tn:customerId", customerId);
        doc.setPropertyValue("tn:envType", "dev");
        doc.setPropertyValue("tn:ready", ready);
        return doc;

    }

    public static Tenant fromDoc(DocumentModel doc) {
        return new Tenant(doc);
    }

}
