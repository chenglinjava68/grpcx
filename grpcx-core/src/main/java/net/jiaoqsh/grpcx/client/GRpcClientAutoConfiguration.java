package net.jiaoqsh.grpcx.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @file: GRpcClientAutoConfiguration
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
@ConditionalOnClass({GRpcChannelFactory.class, DiscoveryClientGRpcChannelFactory.class})
@Configuration
@EnableConfigurationProperties
public class GRpcClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GRpcChannelFactory discoveryClientGRpcChannelFactory() {
        return new DiscoveryClientGRpcChannelFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GRpcClientBeanAnnotationPostProcessor gRpcClientBeanPostProcessor() {
        return new GRpcClientBeanAnnotationPostProcessor();
    }
}
