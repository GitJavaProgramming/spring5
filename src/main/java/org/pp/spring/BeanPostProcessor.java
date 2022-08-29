package org.pp.spring;

import java.util.ArrayList;

public interface BeanPostProcessor {
    public Object beanPostProcessorBeforeInitialization(String beanName, Object bean, ArrayList<Interceptor> interceptors);

    public Object beanPostProcessorAfterInitialization(String beanName, Object bean, ArrayList<Interceptor> interceptors);
}
