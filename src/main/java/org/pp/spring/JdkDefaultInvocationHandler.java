package org.pp.spring;

import org.pp.spring.Interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class JdkDefaultInvocationHandler implements InvocationHandler {
    private final Object bean;
    private final ArrayList<Interceptor> interceptors;

    public JdkDefaultInvocationHandler(Object bean, ArrayList<Interceptor> interceptors) {
        this.bean = bean;
        this.interceptors = interceptors;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Interceptor interceptor : interceptors) {
            interceptor.before();
        }
        Object result = method.invoke(bean, args);
        for (Interceptor interceptor : interceptors) {
            interceptor.after();
        }
        return result;
    }
}
