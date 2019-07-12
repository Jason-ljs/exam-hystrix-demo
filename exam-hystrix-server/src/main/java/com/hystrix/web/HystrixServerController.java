package com.hystrix.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName HystrixServerController
 * @Description: 服务接口
 * @Author 小松
 * @Date 2019/7/11
 **/
@RestController
public class HystrixServerController {

    //被调用的服务接口
    @RequestMapping("testServer")
    public String testServer() throws InterruptedException {
//        Thread.sleep(4000);
        return "服务正常";
    }

}
