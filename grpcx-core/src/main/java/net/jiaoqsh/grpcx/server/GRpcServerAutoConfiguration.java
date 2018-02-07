package net.jiaoqsh.grpcx.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @file: GRpcServerAutoConfiguration
 * @author: jiaoqsh
 * @since: 2018/02/06
 */
@ConditionalOnBean(annotation = GRpcService.class)
@Configuration
@EnableConfigurationProperties
public class GRpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GRpcServerProperties grpcServerProperties() {
        return new GRpcServerProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public GRpcServerBoot grpcServerBoot(GRpcServerProperties properties) {
        return new GRpcServerBoot(properties);
    }
}
