package org.nuxeo.micro.repo.service.core.impl;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import org.apache.commons.lang3.StringUtils;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GrpcInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcInterceptor.class);
    public static final Metadata.Key<String> TENANTID_METADATA_KEY = Metadata.Key.of("tenantId",
            ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> TENANT_ID_KEY = Context.key("tenantId");

    @SuppressWarnings("rawtypes")
    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };

    @SuppressWarnings("unchecked")
    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String tenantId = headers.get(TENANTID_METADATA_KEY);

        Context ctx = Context.current();
        if (StringUtils.isNotBlank(tenantId)) {
            ctx = ctx.withValue(TENANT_ID_KEY, tenantId);
        } else {
            log.error("No tenantId in headers");

            call.close(Status.INVALID_ARGUMENT.withDescription("No tenantId in headers"), headers);
            return NOOP_LISTENER;
        }

        return Contexts.interceptCall(ctx, call, headers, next);
    }

}
