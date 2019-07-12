package com.hystrix.web;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(name = "SERVER",fallback = HystrixInterfaceImpl.class)
public interface HystrixInterface {

    /**
     * 通过feign调用服务中的请求
     * 当服务不可用或超时等异常则进入HystrixInterfaceImpl
     * @return
     */

    @RequestMapping("testServer")
    public String testClient();

}
