package org.nuxeo.micro.repo.provider.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.parser.DslParser;
import org.nuxeo.micro.dsl.parser.DslParserImpl;
import org.nuxeo.micro.repo.provider.TenantSchemaUrlResolver;
import org.nuxeo.micro.repo.provider.impl.SchemaFeature.FieldsDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSLSchemaManagerProvider extends CoreSchemaManagerProvider {

    public static final Logger log = LoggerFactory.getLogger(DSLSchemaManagerProvider.class);
    protected DslParser dslparser;
    private TenantSchemaUrlResolver urlResolver;

    public DSLSchemaManagerProvider(TenantSchemaUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
        dslparser = new DslParserImpl();
        ((DslParserImpl) dslparser).init();
    }

    @Override
    public SchemaManager getForTenant(String tenantId) {
        SchemaManagerImpl sm = (SchemaManagerImpl) super.getForTenant(tenantId);

        try {
            URL resource = urlResolver.getForTenant(tenantId);

            if (resource != null) {
                String dsl = IOUtils.toString(resource.openStream(), Charset.defaultCharset());
                Map<String, Object> ast = dslparser.getAbstractSyntaxTree(dsl);


                DslModel model = DslModel.builder().with(SchemaFeature.class, DocumentTypeFeature.class).build();
                model.visit(ast);
                SchemaFeature schemaFeature = model.getFeature(SchemaFeature.class);

                schemaFeature.getShemaBindings().forEach(sb -> {
                    String schemaName = sb.name;
                    try {
                        File schemaFile = buildXsdForShema(tenantId, sm.getSchemasDir(), schemaName,
                                schemaFeature.getFieldDefs().get(schemaName));
                        sb.src = schemaFile.getAbsolutePath();
                    } catch (IOException e) {
                        log.warn("Unable to read XSD for schema {}", schemaName);
                    }
                });

                schemaFeature.getShemaBindings().stream().forEach(sm::registerSchema);

                DocumentTypeFeature feature = model.getFeature(DocumentTypeFeature.class);
                feature.getDocTypes().stream().forEach(sm::registerDocumentType);
                sm.flushPendingsRegistration();
            }
        } catch (IOException e) {
            log.warn("Unable to read configuration file for tenant [{}]", tenantId, e);
        }

        return sm;
    }

    private File buildXsdForShema(String tenantId, File schemaDir, String schemaName, FieldsDef fields)
            throws IOException {

        try {
            Document doc = loadXsdTemplate();

            String schemaUri = String.format("http://www.nuxeo.org/ecm/project/schemas/%s/%s", tenantId, schemaName);
            Element root = doc.getRootElement();
            // local namespace
            root.addAttribute("targetNamespace", schemaUri);
            root.addNamespace("nxs", schemaUri);

            for (SchemaFeature.Field field : fields.getFields()) {
                Element elem = root.addElement(QName.get("xs:element"));
                String name = field.getName();
                elem.addAttribute("name", name);
                String type = field.getType();
                elem.addAttribute("type", toXSDType(type));
            }
            File file = new File(FileUtils.getTempDirectory(), String.format("%s_%s.xsd", tenantId, schemaName));

            FileWriter fw = new FileWriter(file);
            try {
                XMLWriter writer = new XMLWriter(fw, OutputFormat.createPrettyPrint());
                writer.write(doc);
            } finally {
                fw.close();
            }
            return file;

        } catch (DocumentException e) {
            throw new IOException("Unable to read XSD schema file");
        }
    }

    private String toXSDType(String type) {
        if ("String".equalsIgnoreCase(type)) {
            return "xs:string";
        } else if ("Integer".equalsIgnoreCase(type)) {
            return "xs:integer";
        } else if ("Double".equalsIgnoreCase(type)) {
            return "xs:double";
        } else if ("Date".equalsIgnoreCase(type)) {
            return "xs:date";
        } else if ("Boolean".equalsIgnoreCase(type)) {
            return "xs:boolean";
        } else {
            throw new IllegalArgumentException("Unknown type : " + type);
        }
    }

    private Document loadXsdTemplate() throws DocumentException, IOException {
        URL resource = getClass().getClassLoader().getResource("templates/schema-templates.xsd");

        try (InputStream in = resource.openStream()) {
            return new SAXReader().read(in);
        }


    }
}
