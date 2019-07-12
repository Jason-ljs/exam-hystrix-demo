package com.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName HystrixServerApplication
 * @Description: 服务启动类
 * @Author 小松
 * @Date 2019/7/11
 **/
@SpringBootApplication
@EnableEurekaClient
public class HystrixServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixServerApplication.class,args);
    }

}
