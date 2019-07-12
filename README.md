# Feign使用Hystrix实现断路器简单案例 

断路器的作用是为了保护系统，控制故障范围不再扩大

为了保证系统的高可用，一般我们都会将单个服务进行集群部署，由于网络原因或者其他原因导致单个服务出现问题，调用这个服务时就会线程阻塞，若此时有大量请求涌入，Servlet容器线程资源被消耗完毕，就会导致服务瘫痪。服务与服务之间存在依赖性，故障会传播，会对整个服务器系统造成灾难性的严重后果，这就是服务故障的**雪崩**效应。

Feign中已经集成了Hystrix，所以我们使用Feign中的Hystrix就可以了。

1. 首先创建spring cloud 项目

   引入pom

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
   
       <groupId>com.hystrix</groupId>
       <artifactId>exam-hystrix-demo</artifactId>
       <packaging>pom</packaging>
       <version>1.0-SNAPSHOT</version>
       <modules>
           <module>exam-hystrix-eureka-server</module>
           <module>exam-hystrix-server</module>
           <module>exam-hystrix-client</module>
       </modules>
   
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>2.0.3.RELEASE</version>
       </parent>
   
       <dependencyManagement>
           <dependencies>
               <dependency>
                   <groupId>org.springframework.cloud</groupId>
                   <artifactId>spring-cloud-dependencies</artifactId>
                   <version>Finchley.SR2</version>
                   <scope>import</scope>
                   <type>pom</type>
               </dependency>
           </dependencies>
       </dependencyManagement>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-test</artifactId>
           </dependency>
       </dependencies>
   	
   </project>
   ```

2. 我们需要创建一个注册中心，我们用Eureka

   引入pom

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>exam-hystrix-demo</artifactId>
           <groupId>com.hystrix</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>exam-hystrix-eureka-server</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

   创建配置文件

   ```perl
   #服务的端口号
   server.port=8080
   #服务的名字
   spring.application.name=TEST-EUREKA-SERVER
   
   #配置数据复制的peer节点
   eureka.client.service-url.defaultZone= http://localhost:8080/eureka
   #关闭自我保护
   eureka.server.enable-self-preservation=false
   #不注册自己到Eureka注册中心
   eureka.client.register-with-eureka=false
   #配置不获取注册信息
   eureka.client.fetch-registry=false
   ```

   启动类

   ```java
   package com.hystrix;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
   
   /**
    * @ClassName EurekaServerApplication
    * @Description: 注册中心启动类
    * @Author 小松
    * @Date 2019/7/11
    **/
   @SpringBootApplication
   @EnableEurekaServer
   public class EurekaServerApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(EurekaServerApplication.class,args);
       }
   
   }
   ```

3. 创建服务端服务，为了简单我们只创建一个服务

   引入pom

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>exam-hystrix-demo</artifactId>
           <groupId>com.hystrix</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>exam-hystrix-server</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

   配置文件

   ```perl
   #服务端口
   server.port=8081
   #服务名称
   spring.application.name=SERVER
   #注册中心地址
   eureka.client.service-url.defaultZone= http://localhost:8080/eureka
   ```

   启动类

   ```java
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
   ```

   创建一个控制层接口，供客户端调用

   ```java
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
   //      Thread.sleep(4000);   测试超时保护用
           return "服务正常";
       }
   
   }
   ```

4. 创建客户端

   引入pom

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>exam-hystrix-demo</artifactId>
           <groupId>com.hystrix</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>exam-hystrix-client</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-openfeign</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
               <version>2.0.3.RELEASE</version>
           </dependency>
       </dependencies>
   
   </project>
   ```

   配置文件

   ```perl
   #服务端口号
   server.port=8085
   #服务名称
   spring.application.name=client
   #注册中心地址
   eureka.client.service-url.defaultZone= http://localhost:8080/eureka
   
   #开启hystrix
   feign.hystrix.enabled=true
   
   #配置ribbon的连接超时时间，因为feign默认调用ribbon实现负载均衡的，所以需要配置
   #ribbon.ReadTimeout=5000
   #ribbon.ConnectTimeout=5000
   #开启hystrix连接超时配置
   #hystrix.command.default.execution.timeout.enabled=true
   
   #连接超过10秒后,启用hystrix进行容错
   #hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
   ```

   启动类

   ```java
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
   ```

   feign调用服务接口

   ```java
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
   
   ```

   容错实现类

   ```java
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
   
   ```

   客户端控制层接口

   ```java
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
   
   ```

5. 到此创建完毕

6. 启动注册中心服务，启动服务，启动客户端服务

7. 我们在浏览器地址中输入 `http://localhost:8085/test` 可以正常访问到服务

8. 我们模拟服务不可用的状态，将服务端停掉，再次访问

9. 可以看到，经过hytrix进入了容错

10. ok，到此就结束了一个非常间的小案例，敏感的同学可以发现，我注释掉一个超时的代码和配置，有兴趣的话可以研究一下，超时进入容错

博客链接：https://blog.csdn.net/weixin_43650254/article/details/95620622
