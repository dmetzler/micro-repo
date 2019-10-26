package org.nuxeo.micro.repo.provider.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DocumentTypeFeature;
import org.nuxeo.micro.dsl.features.SchemaFeature;
import org.nuxeo.micro.dsl.parser.DslParser;
import org.nuxeo.micro.dsl.parser.DslParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSLSchemaManagerProvider extends CoreSchemaManagerProvider {

    public static final Logger log = LoggerFactory.getLogger(DSLSchemaManagerProvider.class);
    protected DslParser dslparser;

    public DSLSchemaManagerProvider() {
        dslparser = new DslParserImpl();
        ((DslParserImpl) dslparser).init();
    }

    @Override
    public SchemaManager getForTenant(String tenantId) {
        SchemaManagerImpl sm = (SchemaManagerImpl) super.getForTenant(tenantId);

        try {
            URL resource = getClass().getClassLoader().getResource(tenantId + ".nxl");

            if (resource != null) {
                File file = new File(resource.getFile());
                String dsl = FileUtils.readFileToString(file, Charset.defaultCharset());
                DslModel model = dslparser.parse(dsl);

                SchemaFeature schemaFeature = model.getFeature(SchemaFeature.class);
                schemaFeature.getShemaBindings().stream().forEach(sm::registerSchema);

                DocumentTypeFeature feature = model.getFeature(DocumentTypeFeature.class);
                feature.getDocTypes().stream().forEach(sm::registerDocumentType);
                sm.flushPendingsRegistration();
            }
        } catch (IOException e) {
            log.warn("Unable to read configuration file for tenant [{}]", tenantId);
        }

        return sm;
    }
}
