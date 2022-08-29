package org.pp.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements BeanFactory {

    private final ArrayList<Interceptor> interceptors = new ArrayList<>(); // 拦截器
    private Class config;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); // 单例池
    private ArrayList<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext(Class config) {
        this.config = config;
        refresh();
    }

    private void refresh() {
        // 扫描 缓存
        scan();
        // 缓存单例bean
        cacheSingletonBeans();
    }

    private void cacheSingletonBeans() {
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private void scan() {
        if (config.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) config.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value(); // 扫描路径
            path = path.replace(".", "/");

            ClassLoader classLoader = ApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path/*相对路径*/);
            File file = new File(resource.getFile());
//            System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                Arrays.stream(files).forEach(f -> {
                    String fileName = f.getAbsolutePath();
//                    System.out.println(fileName);
                    if (fileName.endsWith(".class")) {
                        String clazzName = fileName.substring(fileName.indexOf("org"), fileName.indexOf(".class")); // 得到类的全限定名
                        clazzName = clazzName.replace("\\", ".");
//                        System.out.println(clazzName);
                        try {
                            Class<?> clazz = classLoader.loadClass(clazzName); // 得到Class对象
                            if (clazz.isAnnotationPresent(Component.class)) {

                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.getConstructor().newInstance();
                                    beanPostProcessors.add(instance);
                                }

                                if (Interceptor.class.isAssignableFrom(clazz)) {
                                    Interceptor interceptor = (Interceptor) clazz.getConstructor().newInstance();
                                    interceptors.add(interceptor);
                                }

                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();
                                if ("".equals(beanName)) {
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                BeanDefinition beanDefinition = createBeanDefinition(clazz);
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanType();
        Object bean = null;
        try {
//            Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
//            for (Constructor<?> constructor : declaredConstructors) {
//                if (constructor.isAnnotationPresent(Autowired.class)) {
//
//                }
//            }
            bean = clazz.getConstructor().newInstance();
            // 依赖注入
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    f.set(bean, getBean(f.getName()));
                }
            }

            // Aware回调
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            for (BeanPostProcessor p : beanPostProcessors) {
                bean = p.beanPostProcessorBeforeInitialization(beanName, bean, interceptors);
            }

            // 初始化
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }

            for (BeanPostProcessor p : beanPostProcessors) {
                bean = p.beanPostProcessorAfterInitialization(beanName, bean, interceptors);
            }

            // BeanPostProcessor 初始化后 AOP


        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return bean;
    }

    private BeanDefinition createBeanDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanType(clazz);
        if (clazz.isAnnotationPresent(Scope.class)) {
            Scope scope = clazz.getAnnotation(Scope.class);
            if (scope.value().equals("")) {
                beanDefinition.setScope("singleton");
            } else {
                beanDefinition.setScope(scope.value());
            }
        }
        return beanDefinition;
    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if ("singleton".equals(scope)) {
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            }
            return createBean(beanName, beanDefinition);
        }
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        return null;
    }
}
