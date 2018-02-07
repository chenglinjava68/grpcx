package net.jiaoqsh.grpcx.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @file: GRpcClientDemo
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
@SpringBootApplication
public class GRpcClientDemo {

    public static void main(String[] args) {
        System.getProperties().put("server.port", 8082);
        SpringApplication.run(GRpcClientDemo.class,args);
    }

    @Bean
    public GreeterClientService greeterClientService(){
        return new GreeterClientService();
    }

    /*@Bean
    public Channel greeterGrpcChannel() {
        return new DiscoveryClientGRpcChannelFactory().createChannel(GreeterGrpc.class.getName());
    }*/
}
