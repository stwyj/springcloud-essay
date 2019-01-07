# Eureka注册中心——服务注册与发现

## 微服务架构与Spring Cloud

近年来，随着互联网的发展，对于传统系统架构的挑战也越来越严重，这种前提下，微服务架构也应运而生。传统架构的在功能上的耦合，在实例、数据库等方面的共享，不可避免的无法适应当前形势下，市场对产品的快速迭代、数据收集等等方面的需求。而微服务架构首先在业务层面上对传统架构进行了垂直拆分，形成的服务我们称之为微服务。拆分出来的微服务都具有独立维护、扩展、部署的可能性，这样有效的避免了服务间的大量耦合。

在微服务独立部署的过程中，我们需要针对一个服务启动多个实例，这样也带来了一个问题，对于大量部署的微服务我们需要对整个系统的管理、维护、监控等需要很多的组件来维护、治理。

基于此背景，市场上也出现了以Dubbo与Spring Cloud等这样的微服务RPC框架，而相对于Dubbo，Spring Cloud凭借着Spring社区的强大，整合了一整套完整的微服务架构快速构建组件工具，包含了`服务注册发现`、`服务消费`、`配置管理`、`路由网关`、`断路器`、`消息总线`、`链路追踪`等等工具组件，通过这些工具，开发者可以快速的构建自己的微服务系统，同时基于Spring Boot，使构建更加简单。

对于Spring Cloud的学习，我们最好需要具备一定的Spring Boot的知识，有兴趣可以看看[Spring Boot应用与集成分析](https://github.com/stwyj/springboot-essay)

## Eureka注册中心

Spring Cloud Eureka在Spring Cloud架构下主要用作于服务治理，也就是注册中心，其来源与Netflix的一个开源项目，Spring Cloud对它进行了封装整合。

一大堆屁话之后，我们还是来看看基于Eureka怎么构建一个服务注册中心吧。

## eureka-server

首先我们需要创建一个Spring Boot工程，引入Eureka的依赖，如下图所示：

![eureka-server依赖](/ducument/img/eureka-server.png)

### pom.xml（jar依赖）

创建完工程之后，我们的`pom.xml`文件如下，当前Spring Cloud的版本为：`Greenwich.RC2`，Spring Boot的版本为：`2.1.1.RELEASE`。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.stwyj</groupId>
    <artifactId>cloud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>eureka-server</name>
    <description>eureka-server project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.RC2</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
        </repository>
    </repositories>

</project>
```

### application.yml（服务配置）

由于Spring Boot是支持yaml的，实际上yaml配置层次也更加清晰明了，因此在此项目中我们都以yml配置。

```yml
spring:
  application:
    name: eureka-server
server:
  port: 1217
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:1217/eureka/
```

#### 配置解释

1. `eureka.instance.hostname`——该eureka实例的host的名称

2. `eureka.client.registerWithEureka`——由于Eureka服务默认会将自己注册到注册中心，但是我们本身只是一个服务，并不需要这么做，所以这块我们需要设置为`false`

3. `eureka.client.fetchRegistry`——由于该服务是一个注册中心，它并不需要去从自身去获取其他服务的实例，仅仅需要对外提供该自身已注册服务的实例，所以这个也需要设置成`false`

4. `eureka.client.serviceUrl.defaultZone`——该注册中心获取实例地址

### EurekaServerApplication.java（服务开启）

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

对于Eureka服务端的开启，Spring提供了注解的方式，仅仅需要加上注解`@EnableEurekaServer`，这样在此项目中，Eureka服务端已经开启。

### 注册中心页面

在我们启动该项目之后，在浏览器中输入`http://localhost:1217`，我们就可以看到如下页面：

![eureka-server-2](/document/img/eureka-server-2.png)

此时我们的注册中心已经开发完成，是不是很简单？请观察红色框框部分，此处显示无应用。

## eureka-client

在我们创建服务注册中心之后，我们需要在注册中心中注册我们的服务，也就是服务的提供者。

![eureka-client](/document/img/eurela-client.png)

### pom.xml

创建完工程之后，我们的`pom.xml`文件如下，当前Spring Cloud的版本为：`Greenwich.RC2`，Spring Boot的版本为：`2.1.1.RELEASE`。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.stwyj</groupId>
    <artifactId>cloud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>eureka-client</name>
    <description>eureka-client project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.RC2</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
        </repository>
    </repositories>

</project>
```

### application.yml

在服务的提供者配置方面，我们主要配置的是服务的调用信息以及注册中心地址。

```yml
spring:
  application:
    name: service-hello
server:
  port: 8080
eureka:
  client:
    service-url:
      defaultZone: http://localhost:1217/eureka/
```

#### 配置解释

1. 由于Spring Cloud是通过http调用的，所以我们需要指定服务对外的端口为8080，同时指定服务在注册中心中的名称为`service-hello`

2. `eureka.client.serviceUrl.defaultZone`——注册中心地址，用于服务提供者向注册中心注册自身服务。

### EurekaClientApplication.java

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }

}
```

开启服务提供，向注册中心注册服务注解为：`@EnableDiscoveryClient`，我们将服务注册到注册中心也仅仅需要加上该注解。



**注意**

很多地方开启服务提供使用的注解为：`@EnableEurekaClient`，但是此处我们使用的是`@EnableDiscoveryClient`，这个原因是什么呢？

是因为Spring Cloud本身是提供了多种服务注册中心的集成，例如Eureka、Consul、Zookeeper等，我们使用`@EnableEurekaClient`仅仅支持Eureka注册中心，但是经过SPring的封装，`@EnableDiscoveryClient`支持的是所有已封装的服务注册中心。

### HelloController.java

服务提供者提供的服务

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("getServices")
    public List<ServiceInstance> getServices() {

        return discoveryClient.getInstances("service-hello");
    }
}
```

### 注册中心页面

代码编写完成之后，我们启动项目，注意，此处我们将该项目以端口号：`8080`、`8081`、`8082`分别启动，然后我们打开注册中心页面，可以观察到如下：

![注册中心页面](/document/img/eureka-client-2.png)

## 总结

综合以上，我们的服务注册中心已经完成，其实还是很简单的。

对于Eureka服务注册中心，我们从其提供访问接口来看，它提供了两个，一个用于客户端访问由于检测心跳的接口，一个是用于服务消费的时候获取提供者的列表的接口；

基于此分析：

1. 假定我们的服务设置为30秒监测一次心跳，30秒获取一次最新服务列表，那么我们每分钟就得进行4次通信，假定我们在该分布式系统中存在200个服务，那么我们一分钟就有800次请求，如果这个时候我们的注册中心一旦宕机，造成的结果将是灾难性的，所以针对此情况，我们能需要配置一个高可用的Eureka注册中心集群，避免因单台server宕机之后造成系统瘫痪的问题。（后续我们会讲到）

2. 由于服务消费者会从注册中心获取到服务提供者的列表，针对于此，Spring Cloud中提供的一种负载均衡思路——客户端负载均衡。后续服务消费中会用到，我们后期会专门去讲一下服务端负载均衡与客户端负载均衡

## 源码

- [eureka-server 源码参考](/eureka-server)
- [eureka-client 源码参考](/eureka-client)
