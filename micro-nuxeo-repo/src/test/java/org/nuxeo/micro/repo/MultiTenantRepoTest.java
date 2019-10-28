package org.nuxeo.micro.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.micro.NuxeoPrincipalImpl;
import org.nuxeo.micro.repo.provider.DocumentBlobManagerProvider;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;
import org.nuxeo.micro.repo.provider.UIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.DSLSchemaManagerProvider;
import org.nuxeo.micro.repo.provider.impl.DefaultUIDGeneratorServiceProvider;
import org.nuxeo.micro.repo.provider.impl.MockDocumentBloblManagerProvider;
import org.nuxeo.micro.repo.provider.impl.MongoDBRepositoryProviderImpl;
import org.nuxeo.runtime.jtajca.JtaActivator;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class MultiTenantRepoTest {

    RepoConfigurationFactory factory;

    NuxeoPrincipalImpl principal;

    private JtaActivator jta;

    @Before
    public void doBefore() {
        principal = new NuxeoPrincipalImpl("user1", "tenant1");
        principal.setAdministrator(true);

        // Should be built by an Injection Manager (HK2, Guice....)
        SchemaManagerProvider schemaManagerProvider = new DSLSchemaManagerProvider();
        DocumentBlobManagerProvider documentBlobManagerProvider = new MockDocumentBloblManagerProvider();
        UIDGeneratorServiceProvider uidGenProvider = new DefaultUIDGeneratorServiceProvider();
        factory = new RepoConfigurationFactory(
                new MongoDBRepositoryProviderImpl(schemaManagerProvider, documentBlobManagerProvider, uidGenProvider));

        // Activate JTA
        jta = new JtaActivator();
        jta.activate();
        TransactionHelper.startTransaction(3600);
    }

    @After
    public void doAfter() {
        TransactionHelper.commitOrRollbackTransaction();
        jta.deactivate();
    }

    @Test
    public void can_get_several_core_session() throws Exception {

        // Given the repository for tenant1 which has basic configuration
        RepoConfiguration repoConfiguration = factory.getConfiguration("tenant1");
        CoreSessionService css = repoConfiguration.getCoreSessionService();

        try (CloseableCoreSession session = css.createCoreSession(principal)) {
            DocumentModel doc = session.createDocumentModel("/", "test", "Folder");
            doc.setPropertyValue("dc:title", "Test");
            doc = session.createDocument(doc);
            session.save();

            // When I try to create a Book
            try {
                doc = session.createDocumentModel("/test", "Book", "Book");
                fail("Should not be able to create a book");
            } catch (IllegalArgumentException e) {
                // Then it should FAIL
            }

            DocumentModelList docs = session.query("SELECT * FROM Folder");
            docs.forEach(System.out::println);

        }

        // Given the repository for tenant2 which has specific configuration
        repoConfiguration = factory.getConfiguration("tenant2");
        css = repoConfiguration.getCoreSessionService();

        try (CloseableCoreSession session = css.createCoreSession(principal)) {
            // When I create a Book
            DocumentModel doc = session.createDocumentModel("/", "test", "Book");
            doc.setPropertyValue("dc:title", "Test");
            doc.setPropertyValue("bk:isbn", "isbn");
            doc = session.createDocument(doc);
            session.save();

            // Then the book is created
            doc = session.getDocument(new PathRef("/test"));
            assertThat(doc).isNotNull();
            assertThat(doc.getPropertyValue("dc:title")).isEqualTo("Test");
            assertThat(doc.getPropertyValue("bk:isbn")).isEqualTo("isbn");

            DocumentModelList docs = session.query("SELECT * FROM Book");
            docs.forEach(System.out::println);

        }

    }
}
