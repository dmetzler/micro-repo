package org.nuxeo.micro.repo.service.grpc;

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
public final class SessionGrpc {

  private SessionGrpc() {}

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

  public static final String SERVICE_NAME = "NuxeoClient.Session";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.nuxeo.micro.repo.service.grpc.DocumentRequest,
      org.nuxeo.micro.repo.service.grpc.Document> getGetDocumentMethod;

  public static io.grpc.MethodDescriptor<org.nuxeo.micro.repo.service.grpc.DocumentRequest,
      org.nuxeo.micro.repo.service.grpc.Document> getGetDocumentMethod() {
    io.grpc.MethodDescriptor<org.nuxeo.micro.repo.service.grpc.DocumentRequest, org.nuxeo.micro.repo.service.grpc.Document> getGetDocumentMethod;
    if ((getGetDocumentMethod = SessionGrpc.getGetDocumentMethod) == null) {
      synchronized (SessionGrpc.class) {
        if ((getGetDocumentMethod = SessionGrpc.getGetDocumentMethod) == null) {
          SessionGrpc.getGetDocumentMethod = getGetDocumentMethod = 
              io.grpc.MethodDescriptor.<org.nuxeo.micro.repo.service.grpc.DocumentRequest, org.nuxeo.micro.repo.service.grpc.Document>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "NuxeoClient.Session", "getDocument"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.service.grpc.DocumentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.nuxeo.micro.repo.service.grpc.Document.getDefaultInstance()))
                  .setSchemaDescriptor(new SessionMethodDescriptorSupplier("getDocument"))
                  .build();
          }
        }
     }
     return getGetDocumentMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SessionStub newStub(io.grpc.Channel channel) {
    return new SessionStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SessionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SessionBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SessionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SessionFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static SessionVertxStub newVertxStub(io.grpc.Channel channel) {
    return new SessionVertxStub(channel);
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static abstract class SessionImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.service.grpc.DocumentRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.service.grpc.Document> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDocumentMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDocumentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.nuxeo.micro.repo.service.grpc.DocumentRequest,
                org.nuxeo.micro.repo.service.grpc.Document>(
                  this, METHODID_GET_DOCUMENT)))
          .build();
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class SessionStub extends io.grpc.stub.AbstractStub<SessionStub> {
    public SessionStub(io.grpc.Channel channel) {
      super(channel);
    }

    public SessionStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SessionStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SessionStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.service.grpc.DocumentRequest request,
        io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.service.grpc.Document> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class SessionBlockingStub extends io.grpc.stub.AbstractStub<SessionBlockingStub> {
    public SessionBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public SessionBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SessionBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SessionBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public org.nuxeo.micro.repo.service.grpc.Document getDocument(org.nuxeo.micro.repo.service.grpc.DocumentRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDocumentMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class SessionFutureStub extends io.grpc.stub.AbstractStub<SessionFutureStub> {
    public SessionFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public SessionFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SessionFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SessionFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.nuxeo.micro.repo.service.grpc.Document> getDocument(
        org.nuxeo.micro.repo.service.grpc.DocumentRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request);
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static abstract class SessionVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.service.grpc.DocumentRequest request,
        io.vertx.core.Promise<org.nuxeo.micro.repo.service.grpc.Document> response) {
      asyncUnimplementedUnaryCall(getGetDocumentMethod(), SessionGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDocumentMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                org.nuxeo.micro.repo.service.grpc.DocumentRequest,
                org.nuxeo.micro.repo.service.grpc.Document>(
                  this, METHODID_GET_DOCUMENT)))
          .build();
    }
  }

  /**
   * <pre>
   * The Session service definition.
   * </pre>
   */
  public static final class SessionVertxStub extends io.grpc.stub.AbstractStub<SessionVertxStub> {
    public SessionVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public SessionVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SessionVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SessionVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gets a document
     * </pre>
     */
    public void getDocument(org.nuxeo.micro.repo.service.grpc.DocumentRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<org.nuxeo.micro.repo.service.grpc.Document>> response) {
      asyncUnaryCall(
          getChannel().newCall(getGetDocumentMethod(), getCallOptions()), request, SessionGrpc.toObserver(response));
    }
  }

  private static final int METHODID_GET_DOCUMENT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SessionImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SessionImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DOCUMENT:
          serviceImpl.getDocument((org.nuxeo.micro.repo.service.grpc.DocumentRequest) request,
              (io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.service.grpc.Document>) responseObserver);
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
    private final SessionVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(SessionVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DOCUMENT:
          serviceImpl.getDocument((org.nuxeo.micro.repo.service.grpc.DocumentRequest) request,
              (io.vertx.core.Promise<org.nuxeo.micro.repo.service.grpc.Document>) io.vertx.core.Promise.<org.nuxeo.micro.repo.service.grpc.Document>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<org.nuxeo.micro.repo.service.grpc.Document>) responseObserver).onNext(ar.result());
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

  private static abstract class SessionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SessionBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.nuxeo.micro.repo.service.grpc.NuxeoClientProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Session");
    }
  }

  private static final class SessionFileDescriptorSupplier
      extends SessionBaseDescriptorSupplier {
    SessionFileDescriptorSupplier() {}
  }

  private static final class SessionMethodDescriptorSupplier
      extends SessionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SessionMethodDescriptorSupplier(String methodName) {
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
      synchronized (SessionGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SessionFileDescriptorSupplier())
              .addMethod(getGetDocumentMethod())
              .build();
        }
      }
    }
    return result;
  }
}
