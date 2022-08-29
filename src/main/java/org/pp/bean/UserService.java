package org.pp.bean;

import org.pp.spring.*;

@Scope("prototype")
@Component("userService")
public class UserService implements UserInterface, BeanNameAware, InitializingBean {

    @Autowired
    private OrderService orderService;

    private String beanName;

//    public UserService(OrderService orderService) {
//        this.orderService = orderService;
//    }

    @Transactional
    public String say(String msg) {
        System.out.println(orderService);
        System.out.println(beanName);
        return "你好：" + msg;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet");
    }
}
