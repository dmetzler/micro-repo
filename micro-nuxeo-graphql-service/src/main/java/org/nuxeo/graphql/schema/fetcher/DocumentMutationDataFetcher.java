package org.nuxeo.graphql.schema.fetcher;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.core.schema.types.primitives.BooleanType;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.schema.types.primitives.DoubleType;
import org.nuxeo.ecm.core.schema.types.primitives.IntegerType;
import org.nuxeo.ecm.core.schema.types.primitives.LongType;
import org.nuxeo.ecm.core.schema.types.primitives.StringType;
import org.nuxeo.graphql.descriptors.AliasDescriptor;
import org.nuxeo.graphql.schema.NuxeoGQLSchemaManager;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.Document.Property;
import org.nuxeo.micro.repo.proto.Document.Property.Builder;
import org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty;
import org.nuxeo.micro.repo.proto.DocumentCreationRequest;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.NuxeoContext;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

public class DocumentMutationDataFetcher extends AbstractDataFetcher<Document> {

    public enum Mode {
        CREATE, UPDATE, DELETE
    }

    private String targetDocType;

    private Mode mode;

    private NuxeoGQLSchemaManager sm;

    public DocumentMutationDataFetcher(String docType, Mode mode, NuxeoGQLSchemaManager sm) {
        this.targetDocType = docType;
        this.mode = mode;
        this.sm = sm;
    }

    @Override
    public void get(DataFetchingEnvironment environment, Promise<Document> future) {

        NuxeoContext ctx = environment.getContext();

        ctx.session(sr -> {
            if (sr.succeeded()) {
                NuxeoCoreSessionVertxStub session = sr.result();
                getOrCreateDocument(environment, session, sdocr -> {
                    if (sdocr.succeeded()) {
                        org.nuxeo.micro.repo.proto.Document.Builder docBuilder = Document.newBuilder(sdocr.result());

                        hydrateDocArguments(environment, docBuilder);

                        switch (mode) {
                        case UPDATE:
                            session.updateDocument(docBuilder.build(), future);
                            break;
                        case DELETE:
                            session.deleteDocument(docBuilder.build(), future);
                            break;
                        case CREATE:
                            String path = environment.getArgument("parentPath");
                            String name = environment.getArgument("name");

                            docBuilder.setType(targetDocType);

                            DocumentCreationRequest req = DocumentCreationRequest.newBuilder()
                                                                                 .setPath(path)
                                                                                 .setName(name)
                                                                                 .setDocument(docBuilder.build())
                                                                                 .build();
                            session.createDocument(req, future);
                            break;
                        default:
                            throw new IllegalArgumentException("Mode is not supported");
                        }
                    } else {
                        future.fail(sdocr.cause());
                    }
                });

            } else {
                future.fail(sr.cause());
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void hydrateDocArguments(DataFetchingEnvironment environment,
            org.nuxeo.micro.repo.proto.Document.Builder docBuilder) {

        DocumentType docType = sm.getSchemaManager().getDocumentType(targetDocType);

        for (Schema schema : docType.getSchemas()) {
            String schemaName = schema.getNamespace().hasPrefix() ? schema.getNamespace().prefix : schema.getName();
            Map<String, Object> dataModelMap = (Map<String, Object>) environment.getArgument(schemaName);
            if (dataModelMap != null) {
                for (Entry<String, Object> entry : dataModelMap.entrySet()) {
                    Field field = schema.getField(entry.getKey());
                    if (field.getType().isSimpleType()) {

                        String propName = String.format("%s:%s", schemaName, entry.getKey());

                        Builder propBuilder = Property.newBuilder();

                        org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty.Builder strValue = ScalarProperty.newBuilder()
                                                                                                                     .setStrValue(
                                                                                                                             (String) entry.getValue());

                        Property prop = propBuilder.addScalarValue(strValue).build();

                        docBuilder.putProperties(propName, prop);
                    }
                }
            }
        }

        for (Entry<String, AliasDescriptor> entry : sm.getAliases().entrySet()) {
            AliasDescriptor alias = entry.getValue();
            if ("prop".equals(alias.type) && alias.targetDoctype.equals(this.targetDocType)) {
                String value = environment.getArgument(alias.name);
                if (value != null) {
                    String propName = alias.args.get(0);

                    Builder propBuilder = Property.newBuilder();

                    org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty.Builder strValue = ScalarProperty.newBuilder()
                                                                                                                 .setStrValue(
                                                                                                                         value);

                    Property prop = propBuilder.addScalarValue(strValue).build();

                    docBuilder.putProperties(propName, prop);
                }
            }
        }
    }

    private void getOrCreateDocument(DataFetchingEnvironment environment, NuxeoCoreSessionVertxStub session,
            Handler<AsyncResult<Document>> completionHandler) {

        switch (mode) {
        case UPDATE:
        case DELETE:
            DocumentRequest.Builder reqBuilder = DocumentRequest.newBuilder();

            reqBuilder.setId(environment.getArgument("id"));
            session.getDocument(reqBuilder.build(), completionHandler);
            break;
        case CREATE:
            if (environment != null) {
                String path = environment.getArgument("parentPath");
                String name = environment.getArgument("name");

                completionHandler.handle(Future.succeededFuture(
                        Document.newBuilder().setParentPath(path).setName(name).setType(targetDocType).build()));
            } else {
                completionHandler.handle(Future.failedFuture("No path and name found in the request"));
            }
            break;
        default:
            throw new IllegalArgumentException("Mode is not supported");
        }
    }

    public graphql.schema.GraphQLFieldDefinition.Builder buildFieldDefinition(NuxeoGQLSchemaManager sm) {
        graphql.schema.GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition();
        fieldBuilder.type(sm.docTypeToGQLType(targetDocType)).dataFetcher(new VertxDataFetcher<>(this::get));

        switch (mode) {
        case CREATE:
            fieldBuilder.name("create" + targetDocType)
                        .argument(newArgument().name("parentPath").type(GraphQLString))
                        .argument(newArgument().name("name").type(GraphQLString));
            addPropertyFields(fieldBuilder, sm);
            break;
        case UPDATE:
            fieldBuilder.name("update" + targetDocType)//
                        .argument(newArgument().name("id").type(GraphQLString));
            addPropertyFields(fieldBuilder, sm);
            break;
        case DELETE:
            fieldBuilder.name("delete" + targetDocType)

                        .argument(newArgument().name("id").type(GraphQLString));
            break;
        }

        return fieldBuilder;

    }

    private void addPropertyFields(graphql.schema.GraphQLFieldDefinition.Builder fieldBuilder,
            NuxeoGQLSchemaManager sm) {
        // for (Schema schema : sm.getSchemaManager().getDocumentType(targetDocType).getSchemas()) {
        // String name = schema.getNamespace().hasPrefix() ? schema.getNamespace().prefix : schema.getName();
        // GraphQLInputObjectType inputTypeForSchema = inputTypeForSchema(schema.getName(), sm);
        // fieldBuilder.argument(newArgument().name(name).type(inputTypeForSchema));
        //
        // }

        for (Entry<String, AliasDescriptor> entry : sm.getAliases().entrySet()) {
            AliasDescriptor alias = entry.getValue();
            if ("prop".equals(alias.type) && alias.targetDoctype.equals(this.targetDocType)) {
                fieldBuilder.argument(newArgument().name(alias.name).type(GraphQLString));
            }
        }
    }

    private GraphQLInputObjectType inputTypeForSchema(String schemaName, NuxeoGQLSchemaManager sm) {
        Schema s = sm.getSchemaManager().getSchema(schemaName);
        if (!sm.getInputTypeRegistry().containsKey(schemaName)) {
            GraphQLInputObjectType.Builder schemaBuilder = newInputObject().name("ischema_" + schemaName);

            // seems react-admin-simple-graphql needs this
            schemaBuilder.field(newInputObjectField().name("__typename").type(GraphQLString).build());

            for (Field f : s.getFields()) {
                if (!f.getName().getLocalName().matches("[_A-Za-z][_0-9A-Za-z]*")) {
                    continue;
                }

                Type t = f.getType();
                if (t.isSimpleType()) {
                    graphql.schema.GraphQLInputObjectField.Builder fieldBuilder = newInputObjectField().name(
                            f.getName().getLocalName());
                    if (t instanceof StringType) {
                        fieldBuilder.type(GraphQLString);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof BooleanType) {
                        fieldBuilder.type(GraphQLBoolean);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof DateType) {
                        fieldBuilder.type(GraphQLString);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof DoubleType) {
                        fieldBuilder.type(GraphQLFloat);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof IntegerType) {
                        fieldBuilder.type(GraphQLInt);
                        schemaBuilder.field(fieldBuilder.build());
                    } else if (t instanceof LongType) {
                        fieldBuilder.type(GraphQLLong);
                        schemaBuilder.field(fieldBuilder.build());
                    }

                }
            }

            sm.getInputTypeRegistry().put(schemaName, schemaBuilder.build());
        }

        return sm.getInputTypeRegistry().get(schemaName);

    }

}
