package net.jiaoqsh.grpcx.server;

import io.grpc.examples.GreeterGrpc;
import io.grpc.examples.GreeterOuterClass;
import io.grpc.stub.StreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @file: GreeterService
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
@GRpcService(service = GreeterGrpc.class, interceptors = {ServerLogInterceptor.class})
public class GreeterService extends GreeterGrpc.GreeterImplBase {
    private static final Log log = LogFactory.getLog(ServerLogInterceptor.class);

    @Override
    public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
        String message = "Hello " + request.getName();
        final GreeterOuterClass.HelloReply.Builder replyBuilder = GreeterOuterClass.HelloReply.newBuilder().setMessage(message);
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
        log.info("Returning " + message);
    }
}