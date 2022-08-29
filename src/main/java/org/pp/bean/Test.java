package org.pp.bean;

import org.pp.spring.ApplicationContext;

public class Test {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
//        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        userService.say("hello");
//        System.out.println(orderService);

    }
}
