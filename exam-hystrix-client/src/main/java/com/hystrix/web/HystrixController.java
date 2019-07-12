package com.hystrix.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName HystrixController
 * @Description: 客户端控制层
 * @Author 小松
 * @Date 2019/7/11
 **/
@RestController
public class HystrixController {

    @Autowired
    HystrixInterface hystrixInterface;

    @RequestMapping("test")
    public String testClient() {
        String s = hystrixInterface.testClient();
        return s;
    }
}
