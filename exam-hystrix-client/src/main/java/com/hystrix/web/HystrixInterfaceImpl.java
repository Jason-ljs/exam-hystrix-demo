package com.hystrix.web;

import org.springframework.stereotype.Component;

/**
 * @ClassName HystrixInterfaceImpl
 * @Description: 容错
 * @Author 小松
 * @Date 2019/7/11
 **/
@Component
public class HystrixInterfaceImpl implements HystrixInterface {

    @Override
    public String testClient() {
        System.out.println("=======容错=====");
        return "容错";
    }

}
