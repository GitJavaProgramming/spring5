package org.pp.bean;

import org.pp.spring.Component;
import org.pp.spring.Scope;

@Scope
@Component
public class OrderService {
    public void test() {
        System.out.println("orderService");
    }
}
