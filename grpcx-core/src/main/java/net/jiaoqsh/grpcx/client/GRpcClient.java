package net.jiaoqsh.grpcx.client;

import io.grpc.ClientInterceptor;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GRpcClient {

    Class service();

    Class<? extends ClientInterceptor>[] interceptors() default {};

    boolean applyGlobalInterceptors() default true;
}
