package net.jiaoqsh.grpcx.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientLogInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ClientLogInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        log.info(methodDescriptor.getFullMethodName());
        return channel.newCall(methodDescriptor, callOptions);

    }
}
