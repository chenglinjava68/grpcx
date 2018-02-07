package net.jiaoqsh.grpcx.client;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;

import java.util.List;

/**
 * @file: GRpcChannelFactory
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
public interface GRpcChannelFactory {

    Channel createChannel(String name);

    Channel createChannel(String name, List<ClientInterceptor> interceptors);

}
