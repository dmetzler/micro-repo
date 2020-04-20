package org.nuxeo.micro.repo.service.graphql.model;

import java.util.List;
import java.util.stream.Collectors;

import org.nuxeo.micro.repo.proto.Document;
import org.nuxeo.micro.repo.proto.DocumentCreationRequest;
import org.nuxeo.micro.repo.proto.DocumentRequest;
import org.nuxeo.micro.repo.proto.NuxeoCoreSessionGrpc;
import org.nuxeo.micro.repo.proto.QueryRequest;
import org.nuxeo.micro.repo.service.graphql.NuxeoContext;
import org.nuxeo.micro.repo.service.graphql.tenant.ListMetadata;
import org.nuxeo.micro.repo.service.graphql.tenant.Mutation;
import org.nuxeo.micro.repo.service.graphql.tenant.Query;
import org.nuxeo.micro.repo.service.graphql.tenant.QueryBuilder;
import org.nuxeo.micro.repo.service.graphql.tenant.Schema;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;

@Schema("tenant.graphqls")
public class TenantsOperation {

    /**
     *
     */
    private static final String _ALL_LIBRARIES_META = "_allLibrariesMeta";

    @Query("allTenants")
    public void allLibraries(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<List<Tenant>> fut) {

        QueryBuilder qb = new QueryBuilder().from("Tenant");
        TenantFilter filter = new TenantFilter(env.getArgument("filter"));
        fillQueryWithFilter(qb, filter);
        String query = qb.build();
        QueryRequest qr = QueryRequest.newBuilder().setNxql(query).build();
        session.query(qr, docResp -> {
            if (docResp.succeeded()) {
                List<Tenant> result = docResp.result()
                                             .getDocsList()
                                             .stream()
                                             .map(Tenant::from)
                                             .collect(Collectors.toList());

                NuxeoContext ctx = env.getContext();
                ctx.getCache().put(_ALL_LIBRARIES_META, new ListMetadata((long) docResp.result().getTotalCount()));
                fut.complete(result);

            } else {
                fut.fail(docResp.cause());
            }
        });

    }

    @Query("Tenant")
    public void libraryById(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<Tenant> fut) {
        String id = env.getArgument("id");
        DocumentRequest dr = DocumentRequest.newBuilder().setId(id).build();

        session.getDocument(dr, docResp -> {
            if (docResp.succeeded()) {
                fut.complete(Tenant.from(docResp.result()));
            } else {
                fut.fail(docResp.cause());
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Query("_allTenantsMeta")
    public void allTenantsMeta(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<ListMetadata> fut) {

        NuxeoContext ctx = env.getContext();
        if (ctx.getCache().containsKey(_ALL_LIBRARIES_META)) {
            fut.complete((ListMetadata) ctx.getCache().get(_ALL_LIBRARIES_META));
        } else {

            QueryBuilder qb = new QueryBuilder().from("Tenant");
            TenantFilter filter = new TenantFilter(env.getArgument("filter"));
            fillQueryWithFilter(qb, filter);
            String query = qb.build();
            QueryRequest qr = QueryRequest.newBuilder().setNxql(query).build();
            session.query(qr, docResp -> {
                if (docResp.succeeded()) {
                    ListMetadata metadata = new ListMetadata((long) docResp.result().getTotalCount());
                    ctx.getCache().put(_ALL_LIBRARIES_META, metadata);
                    fut.complete(metadata);

                } else {
                    fut.fail(docResp.cause());
                }
            });
        }
    }

    private void fillQueryWithFilter(QueryBuilder qb, TenantFilter filter) {
        // TODO Auto-generated method stub

    }

    @Mutation("createTenant")
    public void newTenant(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<Tenant> fut) {

        String name = env.getArgument("name");
        String schemaDef = env.getArgument("schemaDef");

        Tenant tenant = new Tenant.Builder().name(name).schemaDef(schemaDef).build();

        DocumentRequest docReq = DocumentRequest.newBuilder().setPath("/" + tenant.getName()).build();
        session.getDocument(docReq, dr -> {
            if (dr.succeeded()) {
                fut.fail("Tenant already exists");
            } else {
                Document doc = tenant.toDocument();

                DocumentCreationRequest req = DocumentCreationRequest.newBuilder()
                                                                     .setPath("/")
                                                                     .setName(tenant.getName())
                                                                     .setDocument(doc)
                                                                     .build();

                session.createDocument(req, cr -> {
                    if (cr.succeeded()) {
                        fut.complete(Tenant.from(cr.result()));
                    } else {
                        fut.fail(cr.cause());
                    }
                });
            }
        });

    }

    // // updateTenant(name: String!, city: String!, country: String!): Tenant
    @Mutation("updateTenant")
    public void updateTenant(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<Tenant> fut) {
        String id = env.getArgument("id");
        String schemaDef = env.getArgument("schemaDef");

        DocumentRequest docReq = DocumentRequest.newBuilder().setId(id).build();
        session.getDocument(docReq, dr -> {
            if (dr.succeeded()) {

                Tenant tenant = Tenant.from(dr.result());

                tenant = Tenant.builder(tenant).schemaDef(schemaDef).build();

                session.updateDocument(tenant.toDocument(), ur -> {
                    if (ur.succeeded()) {
                        fut.complete(Tenant.from(ur.result()));
                    } else {
                        fut.fail(ur.cause());
                    }
                });
            } else {
                fut.fail("Tenant does not exist");

            }
        });

    }

    // deleteTenant(TenantId: ID!): Tenant
    @Mutation("deleteTenant")
    public void delete(DataFetchingEnvironment env, NuxeoCoreSessionGrpc.NuxeoCoreSessionVertxStub session,
            Promise<Tenant> fut) {
        String id = env.getArgument("id");

        DocumentRequest docReq = DocumentRequest.newBuilder().setId(id).build();
        session.getDocument(docReq, dr -> {
            if (dr.succeeded()) {

                session.deleteDocument(dr.result(), ur -> {
                    if (ur.succeeded()) {
                        fut.complete(Tenant.from(ur.result()));
                    } else {
                        fut.fail(ur.cause());
                    }
                });
            } else {
                fut.fail("Tenant does not exist");

            }
        });

    }

}
