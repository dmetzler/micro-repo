package org.nuxeo.micro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.CoreService;
import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreSessionService;
import org.nuxeo.ecm.core.api.CoreSessionServiceImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.api.repository.RepositoryManagerImpl;
import org.nuxeo.ecm.core.api.trash.TrashService;
import org.nuxeo.ecm.core.api.validation.DocumentValidationService;
import org.nuxeo.ecm.core.api.versioning.VersioningService;
import org.nuxeo.ecm.core.blob.DocumentBlobManager;
import org.nuxeo.ecm.core.filter.CharacterFilteringService;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.repository.RepositoryFactory;
import org.nuxeo.ecm.core.repository.RepositoryService;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.micro.external.event.EventService;
import org.nuxeo.runtime.jtajca.JtaActivator;
import org.nuxeo.runtime.mongodb.MongoDBComponent;
import org.nuxeo.runtime.mongodb.MongoDBConnectionConfig;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class RepositoryManagerTest {

    private static final int NB_DOCS = 100;

    MongoDBComponent mongoComponent = new MongoDBComponent();

    SchemaManagerImpl sm;

    private NuxeoPrincipalImpl principal;

    private JtaActivator jta;

    private List<String> schemas = Arrays.asList(new String[] { "user", "common", "uid", "dublincore" });

    @Before
    public void doBefore() throws Exception {
        // Activate JTA
        jta = new JtaActivator();
        jta.activate();

        // Init component to hold MongoDB config
        mongoComponent.addDescriptor(getLocalMongoConfig());
        mongoComponent.init();

        // Init SchemManager with fake schema and types
        sm = new SchemaManagerImpl(FileUtils.getTempDirectory());
        buildSchemasAndTypes();

        principal = new NuxeoPrincipalImpl.Builder(sm).name("Administrator").build();
    }

    public SchemaDescriptor[] getSchemaDescriptors(String... schemas) {
        SchemaDescriptor[] result = new SchemaDescriptor[schemas.length];
        for (int i = 0; i < schemas.length; i++) {
            result[i] = new SchemaDescriptor(schemas[i]);
        }
        return result;
    }

    @After
    public void doAfter() {
        jta.deactivate();
    }

    private MongoDBConnectionConfig getLocalMongoConfig() {
        MongoDBConnectionConfig result = new MongoDBConnectionConfig();
        result.server = "localhost:27017";
        result.dbname = "nuxeo";
        result.id = "default";
        return result;

    }

    @Test
    public void can_use_a_core_session() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        TransactionHelper.startTransaction(3600);

        Repository repository = getRepository("default", sm);

        CoreSessionService css = CoreSessionServiceImpl.builder().eventService(mock(EventService.class))
                .securityService(new SecurityService()).versioningService(mock(VersioningService.class))
                .documentValidationService(mock(DocumentValidationService.class))
                .charFilteringService(mock(CharacterFilteringService.class)).trashService(mock(TrashService.class))
                .lifeCycleService(mock(LifeCycleService.class))
                .coreService(new CoreService())
                .build();

        try (CloseableCoreSession session = css.createCoreSession(repository, principal)) {
            assertThat(session).isNotNull();
            for (int i = 0; i < NB_DOCS; i++) {
                DocumentModel doc = session.createDocumentModel("/", "test" + i, "Folder");
                doc.setPropertyValue("dublincore:title", "Test");
                doc = session.createDocument(doc);
            }
            session.save();
            DocumentModel doc = session.getDocument(new PathRef("/test0"));
            assertThat(doc).isNotNull();
            assertThat(doc.getPropertyValue("dublincore:title")).isEqualTo("Test");
            System.out.println(String.format("Created %d documents in %dms", NB_DOCS, System.currentTimeMillis() - currentTimeMillis));

            currentTimeMillis = System.currentTimeMillis();
            for (int i = 0; i < NB_DOCS; i++) {
                session.removeDocument(new PathRef("/test" + i));
            }
            session.save();
            System.out.println(String.format("Deleted %d documents in %dms", NB_DOCS, System.currentTimeMillis() - currentTimeMillis));
        }

        TransactionHelper.commitOrRollbackTransaction();

    }

    private Repository getRepository(String repoName, SchemaManager schemaManager) {
        RepositoryFactory repositoryFactory = new MongoDBRepositoryFactory(repoName,
                mongoComponent.getDatabase("default"), schemaManager, mock(DocumentBlobManager.class));

        org.nuxeo.ecm.core.api.repository.Repository repo = new org.nuxeo.ecm.core.api.repository.Repository("default",
                "default", true, repositoryFactory);

        RepositoryManager rm = new RepositoryManagerImpl();
        rm.addRepository(repo);

        RepositoryService rs = new RepositoryService(rm);
        rs.doCreateRepositories();
        Repository repository = rs.getRepository("default");

        assertThat(repository).isNotNull();
        return repository;
    }

    private void buildSchemasAndTypes() {
        for (String schemaName : schemas) {
            SchemaBindingDescriptor sd = new SchemaBindingDescriptor(schemaName, schemaName);
            sd.src = schemaName + ".xsd";
            sm.registerSchema(sd);
        }

        DocumentTypeDescriptor dtd = new DocumentTypeDescriptor("Document", "Root",
                getSchemaDescriptors("uid", "common"), new String[] { FacetNames.FOLDERISH });

        sm.registerDocumentType(dtd);

        dtd = new DocumentTypeDescriptor("Document", "Folder", getSchemaDescriptors("uid", "common", "dublincore"),
                new String[] {});
        sm.registerDocumentType(dtd);

        sm.flushPendingsRegistration();
    }

}
