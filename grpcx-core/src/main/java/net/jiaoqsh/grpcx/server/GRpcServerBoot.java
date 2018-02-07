package net.jiaoqsh.grpcx.server;

import io.grpc.*;
import net.jiaoqsh.grpcx.discovery.ZookeeperDiscoveryClient;
import net.jiaoqsh.grpcx.interceptor.GRpcGlobalInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @file: GRpcServerBoot
 * @author: jiaoqsh
 * @since: 2018/02/06
 */
public class GRpcServerBoot implements CommandLineRunner, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GRpcServerBoot.class);

    @Autowired
    private AbstractApplicationContext applicationContext;

    @Autowired
    private ZookeeperDiscoveryClient discoveryClient;

    private Server server;

    private final GRpcServerProperties properties;

    private final ServerBuilder<?> serverBuilder;

    public GRpcServerBoot(GRpcServerProperties properties) {
        this.properties = properties;
        this.serverBuilder = ServerBuilder.forPort(properties.getPort());
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting gRPC Server ...");

        Collection<ServerInterceptor> globalInterceptors = getBeanNamesByTypeWithAnnotation(GRpcGlobalInterceptor.class, ServerInterceptor.class)
                .map(name -> applicationContext.getBeanFactory().getBean(name, ServerInterceptor.class))
                .collect(Collectors.toList());

        // find and register all GRpcService-enabled beans
        getBeanNamesByTypeWithAnnotation(GRpcService.class, BindableService.class)
                .forEach(name -> {
                    BindableService srv = applicationContext.getBeanFactory().getBean(name, BindableService.class);
                    ServerServiceDefinition serviceDefinition = srv.bindService();
                    GRpcService gRpcServiceAnn = applicationContext.findAnnotationOnBean(name, GRpcService.class);
                    serviceDefinition = bindInterceptors(serviceDefinition, gRpcServiceAnn, globalInterceptors);
                    serverBuilder.addService(serviceDefinition);
                    try {
                        discoveryClient.addInstance(gRpcServiceAnn.service().getName(), serviceDefinition.getServiceDescriptor(), properties.getPort());
                    } catch (Exception e) {
                        log.error("discoveryClient.addInstance error", e);
                    }
                    log.info("service has been registered :{}", srv.getClass().getName());

                });

        server = serverBuilder.build().start();
        log.info("gRPC Server started, listening on port {}", server.getPort());
        startDaemonAwaitThread();

    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GRpcService gRpcService, Collection<ServerInterceptor> globalInterceptors) {


        Stream<? extends ServerInterceptor> privateInterceptors = Stream.of(gRpcService.interceptors())
                .map(interceptorClass -> {
                    try {
                        return 0 < applicationContext.getBeanNamesForType(interceptorClass).length ?
                                applicationContext.getBean(interceptorClass) :
                                interceptorClass.newInstance();
                    } catch (Exception e) {
                        throw new BeanCreationException("Failed to create interceptor instance.", e);
                    }
                });

        List<ServerInterceptor> interceptors = Stream.concat(
                gRpcService.applyGlobalInterceptors() ? globalInterceptors.stream() : Stream.empty(),
                privateInterceptors)
                .distinct()
                .sorted(serverInterceptorOrderComparator())
                .collect(Collectors.toList());
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }

    private Comparator<Object> serverInterceptorOrderComparator() {
        Function<Object,Boolean> isOrderAnnotated = obj->{
            Order ann = obj instanceof Method ? AnnotationUtils.findAnnotation((Method) obj, Order.class) :
                    AnnotationUtils.findAnnotation(obj.getClass(), Order.class);
            return ann != null;
        };
        return AnnotationAwareOrderComparator.INSTANCE.thenComparing((o1, o2) -> {
            boolean p1 = isOrderAnnotated.apply(o1);
            boolean p2 = isOrderAnnotated.apply(o2);
            return p1 && !p2 ? -1 : p2 && !p1 ? 1 : 0;
        }).reversed();
    }



    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(()->{
            try {
                GRpcServerBoot.this.server.awaitTermination();
            } catch (InterruptedException e) {
                log.error("gRPC server stopped.", e);
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down gRPC server ...");
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        log.info("gRPC server stopped.");
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
