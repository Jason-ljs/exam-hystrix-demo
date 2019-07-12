package com.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @ClassName HystrixClientApplication
 * @Description: 客户端启动类
 * @Author 小松
 * @Date 2019/7/11
 **/
@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients
public class HystrixClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixClientApplication.class,args);
    }

}
