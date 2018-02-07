package net.jiaoqsh.grpcx.client;

import io.grpc.Channel;
import io.grpc.examples.GreeterGrpc;
import io.grpc.examples.GreeterOuterClass;

/**
 * @file: GreeterClient
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
public class GreeterClientService {

    @GRpcClient(service = GreeterGrpc.class, interceptors = ClientLogInterceptor.class)
    private Channel serverChannel;

    public String sayHello(String name) {
        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverChannel);
        GreeterOuterClass.HelloReply response = stub.sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName(name).build());
        return response.getMessage();
    }
}
