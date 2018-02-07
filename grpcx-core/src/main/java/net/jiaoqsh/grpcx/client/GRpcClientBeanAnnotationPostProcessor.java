package net.jiaoqsh.grpcx.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import net.jiaoqsh.grpcx.exception.GRpcxException;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @file: GRpcClientBeanPostProcessor
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
public class GRpcClientBeanAnnotationPostProcessor implements BeanPostProcessor{

    private Map<String, List<Class>> beansToProcess = Maps.newHashMap();

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private GRpcChannelFactory channelFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GRpcClient.class)) {
                    if (!beansToProcess.containsKey(beanName)) {
                        beansToProcess.put(beanName, new ArrayList<>());
                    }
                    beansToProcess.get(beanName).add(clazz);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beansToProcess.containsKey(beanName)) {
            Object target;
            try {
                target = getTargetBean(bean);
            } catch (Exception e) {
                throw new GRpcxException("Failed getTargetBean for create interceptor instance", e);
            }
            for (Class clazz : beansToProcess.get(beanName)) {
                for (Field field : clazz.getDeclaredFields()) {
                    GRpcClient annotation = AnnotationUtils.getAnnotation(field, GRpcClient.class);
                    if (null != annotation) {

                        List<ClientInterceptor> list = Lists.newArrayList();
                        for (Class<? extends ClientInterceptor> clientInterceptorClass : annotation.interceptors()) {
                            ClientInterceptor clientInterceptor;
                            if (beanFactory.getBeanNamesForType(ClientInterceptor.class).length > 0) {
                                clientInterceptor = beanFactory.getBean(clientInterceptorClass);
                            } else {
                                try {
                                    clientInterceptor = clientInterceptorClass.newInstance();
                                } catch (Exception e) {
                                    throw new GRpcxException("Failed to create interceptor instance", e);
                                }
                            }
                            list.add(clientInterceptor);
                        }

                        Channel channel = channelFactory.createChannel(annotation.service().getName(), list);
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, target, channel);
                    }
                }
            }
        }
        return bean;
    }

    private Object getTargetBean(Object bean) throws Exception {
        Object target = bean;
        while (AopUtils.isAopProxy(target)) {
            target = ((Advised) target).getTargetSource().getTarget();
        }
        return target;
    }
}
