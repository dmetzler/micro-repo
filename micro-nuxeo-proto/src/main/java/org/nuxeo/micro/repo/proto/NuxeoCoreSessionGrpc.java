package org.nuxeo.micro.repo.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * The Session service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: nuxeo.proto")
public final class NuxeoCoreSessionGrpc {

  private NuxeoCoreSessionGrpc() {}

  private static <T> io.grpc.stub.StreamObserver<T> toObserver(final io.vertx.core.Handler<io.vertx.core.AsyncResult<T>> handler) {
    return new io.grpc.stub.StreamObserver<T>() {
      private volatile boolean resolved = false;
      @Override
      public void onNext(T value) {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.succeededFuture(value));
        }
      }

      @Override
      public void onError(Throwable t) {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.failedFuture(t));
        }
      }

      @Override
      public void onCompleted() {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.succeededFuture());
        }
      }
    };
  }

  public static final String SERVICE_NAME = "NuxeoClient.NuxeoCoreSession";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentRequest,
      org.nuxeo.micro.repo.proto.Document> getGetDocumentMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentRequest,
      org.nuxeo.micro.repo.proto.Document> getGetDocumentMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentRequest, org.nuxeo.micro.repo.proto.Document> getGetDocumentMethod;
    if ((getGetDocumentMethod = NuxeoCoreSessionGrpc.getGetDocumentMethod) == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        if ((getGetDocumentMethod = NuxeoCoreSessionGrpc.getGetDocumentMethod) == null) {
          NuxeoCoreSessionGrpc.getGetDocumentMethod = getGetDocumentMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.proto.DocumentRequest, org.nuxeo.micro.repo.proto.Document>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.NuxeoCoreSession", "getDocument"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.DocumentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
                  .setSchemaDescriptor(new NuxeoCoreSessionMethodDescriptorSupplier("getDocument"))
                  .build();
          }
        }
     }
     return getGetDocumentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentCreationRequest,
      org.nuxeo.micro.repo.proto.Document> getCreateDocumentMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentCreationRequest,
      org.nuxeo.micro.repo.proto.Document> getCreateDocumentMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.DocumentCreationRequest, org.nuxeo.micro.repo.proto.Document> getCreateDocumentMethod;
    if ((getCreateDocumentMethod = NuxeoCoreSessionGrpc.getCreateDocumentMethod) == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        if ((getCreateDocumentMethod = NuxeoCoreSessionGrpc.getCreateDocumentMethod) == null) {
          NuxeoCoreSessionGrpc.getCreateDocumentMethod = getCreateDocumentMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.proto.DocumentCreationRequest, org.nuxeo.micro.repo.proto.Document>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.NuxeoCoreSession", "createDocument"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.DocumentCreationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
                  .setSchemaDescriptor(new NuxeoCoreSessionMethodDescriptorSupplier("createDocument"))
                  .build();
          }
        }
     }
     return getCreateDocumentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document,
      org.nuxeo.micro.repo.proto.Document> getUpdateDocumentMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document,
      org.nuxeo.micro.repo.proto.Document> getUpdateDocumentMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document, org.nuxeo.micro.repo.proto.Document> getUpdateDocumentMethod;
    if ((getUpdateDocumentMethod = NuxeoCoreSessionGrpc.getUpdateDocumentMethod) == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        if ((getUpdateDocumentMethod = NuxeoCoreSessionGrpc.getUpdateDocumentMethod) == null) {
          NuxeoCoreSessionGrpc.getUpdateDocumentMethod = getUpdateDocumentMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.proto.Document, org.nuxeo.micro.repo.proto.Document>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.NuxeoCoreSession", "updateDocument"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
                  .setSchemaDescriptor(new NuxeoCoreSessionMethodDescriptorSupplier("updateDocument"))
                  .build();
          }
        }
     }
     return getUpdateDocumentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.QueryRequest,
      org.nuxeo.micro.repo.proto.QueryResult> getQueryMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.QueryRequest,
      org.nuxeo.micro.repo.proto.QueryResult> getQueryMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.QueryRequest, org.nuxeo.micro.repo.proto.QueryResult> getQueryMethod;
    if ((getQueryMethod = NuxeoCoreSessionGrpc.getQueryMethod) == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        if ((getQueryMethod = NuxeoCoreSessionGrpc.getQueryMethod) == null) {
          NuxeoCoreSessionGrpc.getQueryMethod = getQueryMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.proto.QueryRequest, org.nuxeo.micro.repo.proto.QueryResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.NuxeoCoreSession", "query"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.QueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.QueryResult.getDefaultInstance()))
                  .setSchemaDescriptor(new NuxeoCoreSessionMethodDescriptorSupplier("query"))
                  .build();
          }
        }
     }
     return getQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document,
      org.nuxeo.micro.repo.proto.Document> getDeleteDocumentMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document,
      org.nuxeo.micro.repo.proto.Document> getDeleteDocumentMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.proto.Document, org.nuxeo.micro.repo.proto.Document> getDeleteDocumentMethod;
    if ((getDeleteDocumentMethod = NuxeoCoreSessionGrpc.getDeleteDocumentMethod) == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        if ((getDeleteDocumentMethod = NuxeoCoreSessionGrpc.getDeleteDocumentMethod) == null) {
          NuxeoCoreSessionGrpc.getDeleteDocumentMethod = getDeleteDocumentMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.proto.Document, org.nuxeo.micro.repo.proto.Document>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.NuxeoCoreSession", "deleteDocument"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.proto.Document.getDefaultInstance()))
                  .setSchemaDescriptor(new NuxeoCoreSessionMethodDescriptorSupplier("deleteDocument"))
                  .build();
          }
        }
     }
     return getDeleteDocumentMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NuxeoCoreSessionStub newStub(io.grpc.Channel channel) {
    return new NuxeoCoreSessionStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NuxeoCoreSessionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new NuxeoCoreSessionBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NuxeoCoreSessionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new NuxeoCoreSessionFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static NuxeoCoreSessionVertxStub newVertxStub(io.grpc.Channel channel) {
    return new NuxeoCoreSessionVertxStub(channel);
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static abstract class NuxeoCoreSessionImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.proto.DocumentRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDocumentMethod(), responseObserver);
    }

    /**
     */
    public void createDocument(org.nuxeo.micro.repo.proto.DocumentCreationRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateDocumentMethod(), responseObserver);
    }

    /**
     */
    public void updateDocument(org.nuxeo.micro.repo.proto.Document request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateDocumentMethod(), responseObserver);
    }

    /**
     */
    public void query(org.nuxeo.micro.repo.proto.QueryRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.QueryResult> responseObserver) {
      asyncUnimplementedUnaryCall(getQueryMethod(), responseObserver);
    }

    /**
     */
    public void deleteDocument(org.nuxeo.micro.repo.proto.Document request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteDocumentMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDocumentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.proto.DocumentRequest,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_GET_DOCUMENT)))
          .addMethod(
            getCreateDocumentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.proto.DocumentCreationRequest,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_CREATE_DOCUMENT)))
          .addMethod(
            getUpdateDocumentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.proto.Document,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_UPDATE_DOCUMENT)))
          .addMethod(
            getQueryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.proto.QueryRequest,
                org.nuxeo.micro.repo.proto.QueryResult>(
                  this, METHODID_QUERY)))
          .addMethod(
            getDeleteDocumentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.proto.Document,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_DELETE_DOCUMENT)))
          .build();
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class NuxeoCoreSessionStub extends io.grpc.stub.AbstractStub<NuxeoCoreSessionStub> {
    public NuxeoCoreSessionStub(io.grpc.Channel channel) {
      super(channel);
    }

    public NuxeoCoreSessionStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NuxeoCoreSessionStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new NuxeoCoreSessionStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.proto.DocumentRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createDocument(org.nuxeo.micro.repo.proto.DocumentCreationRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateDocumentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateDocument(org.nuxeo.micro.repo.proto.Document request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateDocumentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void query(org.nuxeo.micro.repo.proto.QueryRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.QueryResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteDocument(org.nuxeo.micro.repo.proto.Document request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteDocumentMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class NuxeoCoreSessionBlockingStub extends io.grpc.stub.AbstractStub<NuxeoCoreSessionBlockingStub> {
    public NuxeoCoreSessionBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public NuxeoCoreSessionBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NuxeoCoreSessionBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new NuxeoCoreSessionBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public org.nuxeo.micro.repo.proto.Document getDocument(org.nuxeo.micro.repo.proto.DocumentRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDocumentMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.nuxeo.micro.repo.proto.Document createDocument(org.nuxeo.micro.repo.proto.DocumentCreationRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateDocumentMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.nuxeo.micro.repo.proto.Document updateDocument(org.nuxeo.micro.repo.proto.Document request) {
      return blockingUnaryCall(
          getChannel(), getUpdateDocumentMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.nuxeo.micro.repo.proto.QueryResult query(org.nuxeo.micro.repo.proto.QueryRequest request) {
      return blockingUnaryCall(
          getChannel(), getQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.nuxeo.micro.repo.proto.Document deleteDocument(org.nuxeo.micro.repo.proto.Document request) {
      return blockingUnaryCall(
          getChannel(), getDeleteDocumentMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class NuxeoCoreSessionFutureStub extends io.grpc.stub.AbstractStub<NuxeoCoreSessionFutureStub> {
    public NuxeoCoreSessionFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public NuxeoCoreSessionFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NuxeoCoreSessionFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new NuxeoCoreSessionFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.proto.Document> getDocument(
        org.nuxeo.micro.repo.proto.DocumentRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.proto.Document> createDocument(
        org.nuxeo.micro.repo.proto.DocumentCreationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateDocumentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.proto.Document> updateDocument(
        org.nuxeo.micro.repo.proto.Document request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateDocumentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.proto.QueryResult> query(
        org.nuxeo.micro.repo.proto.QueryRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.proto.Document> deleteDocument(
        org.nuxeo.micro.repo.proto.Document request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteDocumentMethod(), getCallOptions()), request);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static abstract class NuxeoCoreSessionVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.proto.DocumentRequest request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document> response) {
      asyncUnimplementedUnaryCall(getGetDocumentMethod(), NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void createDocument(org.nuxeo.micro.repo.proto.DocumentCreationRequest request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document> response) {
      asyncUnimplementedUnaryCall(getCreateDocumentMethod(), NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void updateDocument(org.nuxeo.micro.repo.proto.Document request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document> response) {
      asyncUnimplementedUnaryCall(getUpdateDocumentMethod(), NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void query(org.nuxeo.micro.repo.proto.QueryRequest request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.proto.QueryResult> response) {
      asyncUnimplementedUnaryCall(getQueryMethod(), NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void deleteDocument(org.nuxeo.micro.repo.proto.Document request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document> response) {
      asyncUnimplementedUnaryCall(getDeleteDocumentMethod(), NuxeoCoreSessionGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDocumentMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.proto.DocumentRequest,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_GET_DOCUMENT)))
          .addMethod(
            getCreateDocumentMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.proto.DocumentCreationRequest,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_CREATE_DOCUMENT)))
          .addMethod(
            getUpdateDocumentMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.proto.Document,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_UPDATE_DOCUMENT)))
          .addMethod(
            getQueryMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.proto.QueryRequest,
                org.nuxeo.micro.repo.proto.QueryResult>(
                  this, METHODID_QUERY)))
          .addMethod(
            getDeleteDocumentMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.proto.Document,
                org.nuxeo.micro.repo.proto.Document>(
                  this, METHODID_DELETE_DOCUMENT)))
          .build();
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class NuxeoCoreSessionVertxStub extends io.grpc.stub.AbstractStub<NuxeoCoreSessionVertxStub> {
    public NuxeoCoreSessionVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public NuxeoCoreSessionVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NuxeoCoreSessionVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new NuxeoCoreSessionVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.proto.DocumentRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.proto.Document>> response) {
      asyncUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request, NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void createDocument(org.nuxeo.micro.repo.proto.DocumentCreationRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.proto.Document>> response) {
      asyncUnaryCall(
          getChannel().newCall(getCreateDocumentMethod(), getCallOptions()), request, NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void updateDocument(org.nuxeo.micro.repo.proto.Document request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.proto.Document>> response) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateDocumentMethod(), getCallOptions()), request, NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void query(org.nuxeo.micro.repo.proto.QueryRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.proto.QueryResult>> response) {
      asyncUnaryCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request, NuxeoCoreSessionGrpc.toObserver(response));
    }

    /**
     */
    public void deleteDocument(org.nuxeo.micro.repo.proto.Document request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.proto.Document>> response) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteDocumentMethod(), getCallOptions()), request, NuxeoCoreSessionGrpc.toObserver(response));
    }
  }

  private static final int METHODID_GET_DOCUMENT = 0;
  private static final int METHODID_CREATE_DOCUMENT = 1;
  private static final int METHODID_UPDATE_DOCUMENT = 2;
  private static final int METHODID_QUERY = 3;
  private static final int METHODID_DELETE_DOCUMENT = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NuxeoCoreSessionImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(NuxeoCoreSessionImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DOCUMENT:
          serviceImpl.getDocument((org.nuxeo.micro.repo.proto.DocumentRequest) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver);
          break;
        case METHODID_CREATE_DOCUMENT:
          serviceImpl.createDocument((org.nuxeo.micro.repo.proto.DocumentCreationRequest) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver);
          break;
        case METHODID_UPDATE_DOCUMENT:
          serviceImpl.updateDocument((org.nuxeo.micro.repo.proto.Document) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver);
          break;
        case METHODID_QUERY:
          serviceImpl.query((org.nuxeo.micro.repo.proto.QueryRequest) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.QueryResult>) responseObserver);
          break;
        case METHODID_DELETE_DOCUMENT:
          serviceImpl.deleteDocument((org.nuxeo.micro.repo.proto.Document) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class VertxMethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NuxeoCoreSessionVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(NuxeoCoreSessionVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DOCUMENT:
          serviceImpl.getDocument((org.nuxeo.micro.repo.proto.DocumentRequest) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document>) io.vertx.core.Promise.<org.nuxeo.micro.repo.proto.Document>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_CREATE_DOCUMENT:
          serviceImpl.createDocument((org.nuxeo.micro.repo.proto.DocumentCreationRequest) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document>) io.vertx.core.Promise.<org.nuxeo.micro.repo.proto.Document>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_UPDATE_DOCUMENT:
          serviceImpl.updateDocument((org.nuxeo.micro.repo.proto.Document) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document>) io.vertx.core.Promise.<org.nuxeo.micro.repo.proto.Document>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_QUERY:
          serviceImpl.query((org.nuxeo.micro.repo.proto.QueryRequest) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.proto.QueryResult>) io.vertx.core.Promise.<org.nuxeo.micro.repo.proto.QueryResult>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.QueryResult>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_DELETE_DOCUMENT:
          serviceImpl.deleteDocument((org.nuxeo.micro.repo.proto.Document) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.proto.Document>) io.vertx.core.Promise.<org.nuxeo.micro.repo.proto.Document>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.proto.Document>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class NuxeoCoreSessionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NuxeoCoreSessionBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.nuxeo.micro.repo.proto.NuxeoClientProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NuxeoCoreSession");
    }
  }

  private static final class NuxeoCoreSessionFileDescriptorSupplier
      extends NuxeoCoreSessionBaseDescriptorSupplier {
    NuxeoCoreSessionFileDescriptorSupplier() {}
  }

  private static final class NuxeoCoreSessionMethodDescriptorSupplier
      extends NuxeoCoreSessionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NuxeoCoreSessionMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (NuxeoCoreSessionGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NuxeoCoreSessionFileDescriptorSupplier())
              .addMethod(getGetDocumentMethod())
              .addMethod(getCreateDocumentMethod())
              .addMethod(getUpdateDocumentMethod())
              .addMethod(getQueryMethod())
              .addMethod(getDeleteDocumentMethod())
              .build();
        }
      }
    }
    return result;
  }
}
