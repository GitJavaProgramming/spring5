package org.pp.bean;

import org.pp.spring.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    // @PostConstruct
    @Override
    public Object beanPostProcessorBeforeInitialization(String beanName, Object bean, ArrayList<Interceptor> interceptors) {
        if ("userService".equals(beanName)) {
            System.out.println("before initializing");
        }
        return bean;
    }

    // AOP
    @Override
    public Object beanPostProcessorAfterInitialization(String beanName, Object bean, ArrayList<Interceptor> interceptors) {
        Class<?> clazz = bean.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Transactional.class)) {
                Object proxy = Proxy.newProxyInstance(
                        MyBeanPostProcessor.class.getClassLoader()
                        , bean.getClass().getInterfaces()
                        , new JdkDefaultInvocationHandler(bean, interceptors));
                return proxy;
            }
        }
        return bean;
    }
}
