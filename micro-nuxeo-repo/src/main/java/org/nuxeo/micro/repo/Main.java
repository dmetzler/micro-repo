package org.nuxeo.micro.repo;

import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.NuxeoPrincipalImpl;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.DefaultUIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.MockDocumentBloblManagerProvider;
import org.nuxeo.micro.repo.provider.impl.MongoDBRepositoryProviderImpl;
import org.nuxeo.micro.repo.provider.impl.CoreSchemaManagerProvider;
import org.nuxeo.runtime.jtajca.JtaActivator;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class Main {

    public static void main(String[] args) {
        System.out.println("Start");
        NuxeoPrincipalImpl principal = new NuxeoPrincipalImpl("user1", "tenant1");
        principal.setAdministrator(true);

        // Should be built by an Injection Manager (HK2, Guice....)
        SchemaManagerProvider schemaManagerProvider = new CoreSchemaManagerProvider();
        DocumentBlobManagerProvider documentBlobManagerProvider = new MockDocumentBloblManagerProvider();
        UIDGeneratorServiceProvider uidGenProvider = new DefaultUIDGeneratorServiceProvider();
        RepoConfigurationFactory factory = new RepoConfigurationFactory(
                new MongoDBRepositoryProviderImpl(schemaManagerProvider, documentBlobManagerProvider, uidGenProvider));

        // Activate JTA
        JtaActivator jta = new JtaActivator();
        jta.activate();
        TransactionHelper.startTransaction();

        RepoConfiguration repoConfiguration = factory.getConfiguration("tenant1");
        CoreSessionService css = repoConfiguration.getCoreSessionService();

        try (CloseableCoreSession session = css.createCoreSession(principal)) {
            DocumentModel doc = session.createDocumentModel("/", "test", "Folder");
            doc.setPropertyValue("dc:title", "Test");
            doc = session.createDocument(doc);
            session.save();

            doc = session.getDocument(new PathRef("/test"));
            session.removeDocument(new PathRef("/test"));

            session.save();

        }

        TransactionHelper.commitOrRollbackTransaction();
    }

}
