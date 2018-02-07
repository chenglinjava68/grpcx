package net.jiaoqsh.grpcx.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.grpc.*;
import net.jiaoqsh.grpcx.discovery.InstanceDetails;
import net.jiaoqsh.grpcx.discovery.ZookeeperDiscoveryClient;
import net.jiaoqsh.grpcx.exception.GRpcxException;
import net.jiaoqsh.grpcx.interceptor.GRpcGlobalInterceptor;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @file: DiscoveryClientGRpcChannelFactory
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
public class DiscoveryClientGRpcChannelFactory implements GRpcChannelFactory {

    private static final Logger log = LoggerFactory.getLogger(GRpcChannelFactory.class);

    @Autowired
    private AbstractApplicationContext applicationContext;

    @Autowired
    private ZookeeperDiscoveryClient discoveryClient;

    @Override
    public Channel createChannel(String serviceName) {
        return createChannel(serviceName, Lists.newArrayList());
    }

    @Override
    public Channel createChannel(String serviceName, List<ClientInterceptor> interceptors) {
        log.info("createChannel start, serviceName={}", serviceName);
        ServiceInstance<InstanceDetails> serviceInstance;
        ManagedChannel channel;
        try {
            serviceInstance = discoveryClient.getInstanceByRandomStrategy(serviceName);
            channel =  ManagedChannelBuilder.forAddress(serviceInstance.getAddress(), serviceInstance.getPort())
                    .usePlaintext(true)
                    .build();

            List<ClientInterceptor> globalInterceptorList = getBeanNamesByTypeWithAnnotation(GRpcGlobalInterceptor.class, ClientInterceptor.class)
                    .map(name -> applicationContext.getBeanFactory().getBean(name, ClientInterceptor.class))
                    .collect(Collectors.toList());
            Set<ClientInterceptor> interceptorSet = Sets.newHashSet();
            if (globalInterceptorList != null && !globalInterceptorList.isEmpty()) {
                interceptorSet.addAll(globalInterceptorList);
            }
            if (interceptors != null && !interceptors.isEmpty()) {
                interceptorSet.addAll(interceptors);
            }
            return ClientInterceptors.intercept(channel, Lists.newArrayList(interceptorSet));
        } catch (Exception ex) {
            log.error("createChannel error, serviceName={}", serviceName, ex);
            throw new GRpcxException(ex);
        }
    }

    private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<? extends Annotation> annotationType, Class<T> beanType) throws Exception {

        return Stream.of(applicationContext.getBeanNamesForType(beanType))
                .filter(name -> {
                    final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

                    if (!beansWithAnnotation.isEmpty()) {
                        return beansWithAnnotation.containsKey(name);
                    } else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }

                    return false;
                });
    }
}
