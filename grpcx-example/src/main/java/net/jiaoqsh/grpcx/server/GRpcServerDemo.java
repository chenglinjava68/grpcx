package net.jiaoqsh.grpcx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @file: GRpcServerDemo
 * @author: jiaoqsh
 * @since: 2018/02/06
 */
@SpringBootApplication
public class GRpcServerDemo {

    public static void main(String[] args) {
        System.getProperties().put("server.port", 8081);
        SpringApplication.run(GRpcServerDemo.class,args);
    }

    @Bean
    public GreeterService greeterService() {
        return new GreeterService();
    }
}
