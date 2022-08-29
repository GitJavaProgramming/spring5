package org.pp.spring;

public interface BeanFactory {

    Object getBean(String beanName);

    <T> T getBean(Class<T> beanType);
}
