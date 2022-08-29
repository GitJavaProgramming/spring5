package org.pp.bean;

import org.pp.spring.Component;
import org.pp.spring.Interceptor;

@Component
public class MyInterceptor implements Interceptor {
    @Override
    public void before() {
        System.out.println("切面逻辑 执行前");
    }

    @Override
    public void after() {
        System.out.println("切面逻辑 执行后");
    }
}
