package org.nuxeo.graphql.schema.fetcher;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.Document.Property;
import org.nuxeo.micro.repo.proto.Document.Property.Builder;
import org.nuxeo.micro.repo.proto.Document.Property.ScalarProperty;
import org.nuxeo.micro.repo.proto.DocumentCreationRequest;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub;
import org.nuxeo.micro.repo.service.graphql.NuxeoGraphqlContext;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

public class DocumentMutationDataFetcher extends AbstractDataFetcher<Document> {

    public enum Mode {
        CREATE, UPDATE, DELETE
    }

    private String targetDocType;

    private Mode mode;

    public DocumentMutationDataFetcher(String docType, Mode mode) {
        this.targetDocType = docType;
        this.mode = mode;
    }

    @Override
    public void get(DataFetchingEnvironment environment, Promise<Document> future) {
        Map<String, Object> docInputMap = environment.getArgument(targetDocType);

        SchemaManager sm = ((NuxeoGraphqlContext) environment.getContext()).getSchemaManager();
        NuxeoCoreSessionVertxStub session = getSession(environment.getContext());

        getOrCreateDocument(docInputMap, session, sdocr -> {
            if (sdocr.succeeded()) {
                org.nuxeo.micro.repo.proto.Document.Builder docBuilder = sdocr.result().newBuilderForType();

                DocumentType docType = sm.getDocumentType(targetDocType);
                for (Schema schema : docType.getSchemas()) {
                    String schemaName = schema.getNamespace().hasPrefix() ? schema.getNamespace().prefix
                            : schema.getName();
                    Map<String, Object> dataModelMap = (Map<String, Object>) docInputMap.get(schemaName);
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

                switch (mode) {
                case UPDATE:
                    session.updateDocument(docBuilder.build(), future);
                    break;
                case DELETE:
                    session.deleteDocument(docBuilder.build(), future);
                    break;
                case CREATE:
                    String path = (String) docInputMap.get("_path");
                    String name = (String) docInputMap.get("_name");

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

    }

    private void getOrCreateDocument(Map<String, Object> docInputMap, NuxeoCoreSessionVertxStub session,
            Handler<AsyncResult<Document>> completionHandler) {
        String id = (String) docInputMap.get("_id");
        String path = (String) docInputMap.get("_path");
        String name = (String) docInputMap.get("_name");

        switch (mode) {
        case UPDATE:
        case DELETE:
            DocumentRequest.Builder reqBuilder = DocumentRequest.newBuilder();
            if (StringUtils.isNotBlank(id)) {
                reqBuilder.setId(id);
            } else {
                reqBuilder.setPath(path);
            }
            session.getDocument(reqBuilder.build(), completionHandler);
            break;
        case CREATE:
            completionHandler.handle(Future.succeededFuture(
                    Document.newBuilder().setParentPath(path).setName(name).setType(targetDocType).build()));
            break;
        default:
            throw new IllegalArgumentException("Mode is not supported");
        }
    }

}
