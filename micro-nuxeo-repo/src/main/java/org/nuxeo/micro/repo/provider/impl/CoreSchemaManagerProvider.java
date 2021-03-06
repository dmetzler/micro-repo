package org.nuxeo.micro.repo.provider.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.micro.repo.provider.SchemaManagerProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class CoreSchemaManagerProvider implements SchemaManagerProvider {

    private static final List<String> schemas = new ImmutableList.Builder<String>()//
            .add("core-types") //
            .add("base") //
            .add("relation") //
            .add("common") //
            .add("dublincore") //
            .add("uid") //
            .add("file") //
            .add("files") //
            .add("note") //
            .add("domain") //
            .add("relatedtext") //
            .add("publishing") //
            .add("webcontainer") //
            .add("collection") //
            .add("collectionMember") //
            .build();

    private static final List<String> facets = new ImmutableList.Builder<String>()//
            .add("Folderish")//
            .add("Orderable") //
            .add("Versionable") //
            .add("Downloadable") //
            .add("Publishable") //
            .add("PublishSpace") //
            .add("MasterPublishSpace") //
            .add("Commentable") //
            .add("WebView") //
            .add("SuperSpace") //
            .add("HiddenInNavigation") //
            .add("SystemDocument") //
            .add("NotFulltextIndexable") //
            .add("BigFolder") //
            .add("HiddenInCreation") //
            .add("NotCollectionMember") //
            .build();

    private static final Map<String, String> facetsWithSchema = new ImmutableMap.Builder<String, String>()
            .put("HasRelatedText", "relatedtext")//
            .put("Collection", "collection")//
            .put("CollectionMember", "collectionMember")//
            .build();

    @Override
    public SchemaManager getForTenant(String tenantId) {

        SchemaManagerImpl sm = new SchemaManagerImpl(FileUtils.getTempDirectory());
        sm.registerConfiguration(new DefaultTypeConfiguration());
        buildSchemasAndTypes(sm);
        return sm;
    }

    private void buildSchemasAndTypes(SchemaManagerImpl sm) {

        schemas.stream().map(this::buildSchemaDescriptor).forEach(sm::registerSchema);
        facets.stream().map(this::buildFacetDescriptor).forEach(sm::registerFacet);
        facetsWithSchema.entrySet().stream().map(this::buildFacetDescriptor).forEach(sm::registerFacet);


        List<DocumentTypeDescriptor> typeDescriptors = buildDocumentTypesDefinitions();
        typeDescriptors.stream().forEach(sm::registerDocumentType);

        sm.flushPendingsRegistration();
    }

    private List<DocumentTypeDescriptor> buildDocumentTypesDefinitions() {
        return new ImmutableList.Builder<DocumentTypeDescriptor>()
//        <doctype name="Folder" extends="Document">
//        <schema name="common"/>
//        <schema name="dublincore"/>
//        <facet name="Folderish"/>
//        <subtypes>
//          <type>Collection</type>
//          <type>Folder</type>
//          <type>OrderedFolder</type>
//          <type>File</type>
//          <type>Note</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Document")//
                .name("Folder")//
                .schemas("common", "dublincore")//
                .facets("Folderish")//
                .allowedSubtypes("Collection", "Folder", "OrderedFolder", "File", "Note")//
                .build())

//      <doctype name="OrderedFolder" extends="Folder">
//        <facet name="Orderable"/>
//        <subtypes>
//          <type>Folder</type>
//          <type>OrderedFolder</type>
//          <type>File</type>
//          <type>Note</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Document")//
                .name("OrderedFolder")//
                .facets("Orderable")//
                .allowedSubtypes("Folder", "OrderedFolder", "File", "Note")//
                .build())

//
//      <doctype name="HiddenFolder" extends="Folder">
//        <facet name="HiddenInNavigation" />
//      </doctype>

       .add(new DTDBuilder()//
                .parent("Folder")//
                .name("HiddenFolder")//
                .facets("HiddenInNavigation")//
                .build())

//
//      <doctype name="Root" extends="Folder">
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>Domain</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("Root")//
                .facets("NotCollectionMember")//
                .allowedSubtypes("Domain")//
                .build())

        //
//      <doctype name="Relation"> <!-- no extends -->
//        <schema name="relation"/>
//        <schema name="dublincore"/>
//      </doctype>

        .add(new DTDBuilder()//
                .name("Relation")//
                .schemas("relation", "dublincore")//
                .build())

//
//      <doctype name="Domain" extends="Folder">
//        <schema name="domain"/>
//        <facet name="SuperSpace"/>
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>WorkspaceRoot</type>
//          <type>SectionRoot</type>
//          <type>TemplateRoot</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("Domain")//
                .schemas("domain")//
                .facets("SuperSpace", "NotCollectionMember")//
                .allowedSubtypes("WorkspaceRoot", "SectionRoot", "TemplateRoot")//
                .build())

//
//      <doctype name="WorkspaceRoot" extends="Folder">
//        <facet name="SuperSpace"/>
//        <facet name="HiddenInCreation" />
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>Workspace</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("WorkspaceRoot")//
                .facets("SuperSpace", "HiddenInCreation", "NotCollectionMember")//
                .allowedSubtypes("Workspace") //
                .build())

//
//      <doctype name="Workspace" extends="Folder">
//        <!-- for logo -->
//        <schema name="file"/>
//        <schema name="webcontainer"/>
//        <schema name="publishing"/>
//        <!-- the content of webcontainer -->
//        <schema name="files" />
//        <facet name="SuperSpace"/>
//        <subtypes>
//          <type>Collection</type>
//          <type>Workspace</type>
//          <type>Folder</type>
//          <type>OrderedFolder</type>
//          <type>File</type>
//          <type>Note</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("Workspace")//
                .schemas("file", "webcontainer", "publishing", "files") //
                .facets("SuperSpace")//
                .allowedSubtypes("Collection", "Workspace", "Folder", "OrderedFolder", "File", "Note") //
                .build())

//
//      <doctype name="TemplateRoot" extends="Folder">
//        <facet name="SuperSpace"/>
//        <facet name="HiddenInCreation" />
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>Workspace</type>
//        </subtypes>
//      </doctype>
        .add(new DTDBuilder()//
                .parent("Folder").name("TemplateRoot")//
                .facets("SuperSpace", "HiddenInCreation", "NotCollectionMember")//
                .allowedSubtypes("Workspace") //
                .build())

//
//      <doctype name="SectionRoot" extends="Folder">
//        <facet name="SuperSpace"/>
//        <facet name="HiddenInCreation" />
//        <facet name="MasterPublishSpace" />
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>Section</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("SectionRoot")//
                .facets("SuperSpace", "HiddenInCreation", "NotCollectionMember", "MasterPublishSpace")//
                .allowedSubtypes("Section") //
                .build())

//
//      <doctype name="Section" extends="Folder">
//        <!-- for logo -->
//        <schema name="file"/>
//        <facet name="SuperSpace"/>
//        <facet name="PublishSpace" />
//        <subtypes>
//          <type>Section</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("Section")//
                .facets("SuperSpace", "PublishSpace")//
                .allowedSubtypes("Section") //
                .build())
//
//      <doctype name="File" extends="Document">
//        <schema name="common"/>
//        <schema name="file"/>
//        <schema name="dublincore"/>
//        <schema name="uid"/>
//        <schema name="files"/>
//        <facet name="Downloadable"/>
//        <facet name="Versionable"/>
//        <facet name="Publishable"/>
//        <facet name="Commentable"/>
//        <facet name="HasRelatedText"/>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Document")//
                .name("File")//
                .schemas("common", "file", "dublincore", "uid", "files")
                .facets("Downloadable", "Versionable", "Publishable", "Commentable", "HasRelatedText")//
                .build())
//
//      <doctype name="Note" extends="Document">
//        <schema name="common"/>
//        <schema name="note"/>
//        <schema name="uid"/>
//        <schema name="files"/>
//        <schema name="dublincore"/>
//        <facet name="Versionable"/>
//        <facet name="Publishable"/>
//        <facet name="Commentable"/>
//        <facet name="HasRelatedText"/>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Document")//
                .name("Note")//
                .schemas("common", "note", "dublincore", "uid", "files")
                .facets("Versionable", "Publishable", "Commentable", "HasRelatedText")//
                .build())
//
//      <doctype name="Collection" extends="Document">
//        <schema name="uid"/>
//        <facet name="Versionable"/>
//        <facet name="Collection" />
//        <facet name="NotCollectionMember" />
//        <schema name="dublincore" />
//        <schema name="common" />
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Document")//
                .name("Collection")//
                .schemas("uid", "dublincore", "common")//
                .facets("Versionable", "Collection", "NotCollectionMember")//
                .build())
//
//      <doctype name="Collections" extends="Folder">
//        <facet name="NotCollectionMember" />
//        <subtypes>
//          <type>Collection</type>
//        </subtypes>
//      </doctype>

        .add(new DTDBuilder()//
                .parent("Folder")//
                .name("Collections")//
                .allowedSubtypes("Collection")//
                .build())
        .build();
    }

    private SchemaBindingDescriptor buildSchemaDescriptor(String schemaName) {
        SchemaBindingDescriptor sd = new SchemaBindingDescriptor(schemaName, schemaName);
        sd.src = schemaName + ".xsd";
        sd.prefix = "dublincore".equals(schemaName) ? "dc" : schemaName;
        return sd;
    }

    private FacetDescriptor buildFacetDescriptor(String facetName) {
        FacetDescriptor fd = new FacetDescriptor();
        fd.name = facetName;
        fd.schemas = new SchemaDescriptor[] {};
        return fd;
    }

    private FacetDescriptor buildFacetDescriptor(Entry<String, String> facet) {
        FacetDescriptor fd = new FacetDescriptor();
        fd.name = facet.getKey();
        fd.schemas = new SchemaDescriptor[] { new SchemaDescriptor(facet.getValue()) };
        return fd;
    }

    public static class DTDBuilder {

        private String parent;
        private String name;
        private String[] schemas = new String[] {};
        private String[] facets = new String[] {};
        private String[] allowedSubtypes = new String[] {};
        private String[] disallowedSubtypes = new String[] {};

        public DTDBuilder parent(String parentType) {
            this.parent = parentType;
            return this;
        }

        public DTDBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DTDBuilder schemas(String... schemas) {
            this.schemas = schemas;
            return this;
        }

        public DTDBuilder facets(String... facets) {
            this.facets = facets;
            return this;
        }

        public DTDBuilder allowedSubtypes(String... allowedSubtypes) {
            this.allowedSubtypes = allowedSubtypes;
            return this;
        }

        public DTDBuilder disallowedSubtypes(String... disallowedSubtypes) {
            this.disallowedSubtypes = disallowedSubtypes;
            return this;
        }

        public SchemaDescriptor[] getSchemaDescriptors(String... schemas) {
            SchemaDescriptor[] result = new SchemaDescriptor[schemas.length];
            for (int i = 0; i < schemas.length; i++) {
                String name = schemas[i];
                result[i] = new SchemaDescriptor(name);
            }
            return result;
        }

        public DocumentTypeDescriptor build() {
            return new DocumentTypeDescriptor(parent, name, getSchemaDescriptors(schemas), facets, allowedSubtypes,
                    disallowedSubtypes);
        }
    }

}
