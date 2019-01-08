# 服务消费——Ribbon

## spring-cloud-commons

在Spring Cloud中存在一个spring-cloud-commons的包，该包主要提供了独立于组建之外的一些注入服务发现、负载均衡等等接口的共同抽象层，提供了`@EnableDiscoveryClient`和`@LoadBalanced`等注解支持。有时间会专门去讲解一下该包。

在本示例中我们已经使用了`@EnableDiscoveryClient`注解来标注服务的提供者，以及对服务的发现能力。

同时我们使用到了LoadBalancerClient也就是，`@LoadBalanced`该注解的对应的接口LoadBalancerClient接口。

## Ribbon

什么是Ribbon，简单理解，我们可以认为Ribbon提供的是客户端负载均衡的一种分布式组件，是基于`Netflix Ribbon`实现的。

Ribbon本身包括多部分功能，主要分为以下几种：

1. `ServerList`，主要用于存储获取到的注册中心中提供服务的client的列表

2. `ServerListFilter`，主要用于筛选`ServerList`获取到的server列表，获取到实际需要使用的servers

3. `ServerListUpdater`，主要用于去刷新服务列表，当某个server下线等情况下，其能保证在一定时间内去刷新可用的服务列表

4. `IPing`，主要用于验证服务的可用性

5. `ILoadBalancer`，这里就是我们的主要核心，也就是我们的负载均衡主要在这里进行，对获取到的server进行负载均衡之后选择适合的server进行访问

6. `IRule`，这个就是我们在负载均衡的过程中使用到的负载均衡策略

7. `IClientConfig`，`Ribbon`主要用于获取配置信息，`Ribbon`提供了默认的实现（DefaultClientConfigImpl），底层通过Archauis获取配置信息，如果没有配置信息，DefaultClientConfigImpl也为各个配置项设置了默认值。DefaultClientConfigImpl的配置项的格式为：

```text
<clientName>.<nameSpace>.<propertyName>
```
## eureka-consumer

首先我们跟之前一样创建一个eureka-client的工程，如下图所示：

![eureka-consumer](/document/img/eureka-client.png)

我们发现该工程于之前创建的服务提供者`eureka-client`是一致的，这个是因为其实我们服务的消费者实际上也需要去注册中心注册，然后获取到提供者的注册信息，其本质也是一个`client`，也可以对外提供服务。

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
    <name>eureka-consumer</name>
    <description>Demo project for Spring Boot</description>

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

### application.yml（服务配置）

在服务的提供者配置方面，我们主要配置的是服务的调用信息以及注册中心地址。

其配置还是与`eureka-client`一致的

```yml
spring:
  application:
    name: service-consumer
server:
  port: 8083
eureka:
  client:
    service-url:
      defaultZone: http://localhost:1217/eureka/
```

#### 配置解释

1. 由于Spring Cloud是通过http调用的，所以我们需要指定服务对外的端口为8080，同时指定服务在注册中心中的名称为`service-consumer`

2. `eureka.client.serviceUrl.defaultZone`——注册中心地址，用于服务提供者向注册中心注册自身服务。

3. 服务消费者与服务提供者都属于注册中心注册服务，也就是`client`，本质都是一样的，不同地方在于不同维度的定义。

### EurekaConsumerApplication.java

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class EurekaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaConsumerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
```

我们在此定义了`RestTemplate`，用作于服务消费。Spring Cloud中服务之间的通信协议是通过HTTP协议和TCP协议的，所以其调用也就可以使用基于rest的http接口的`RestTemplate`。

### ConsumerController.java

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("serviceConsumer")
    public String serviceConsumer() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("service-hello");
        return restTemplate.getForObject(serviceInstance.getUri().toString() + "/getPort", String.class);
    }
}
```

该类中，我们注入了`Ribbon`基于客户端负载均衡的`LoadBalancerClient`，通过该负载均衡器，我们获取到需要消费的服务`service-hello`，通过`choose`方法获取到该服务的实例，然后我们通过`RestTemplate`直接调用该服务。

### 注册页面

启动项目之后，我们还是打开注册页面，查看注册的实例，如下图：

![eureka-consumer](/document/img/eureka-consumer.png)

### 调用测试

我们在浏览器`http://localhost:8083/serviceConsumer`输入该地址，我们可以看到返回结果一直在`this is a project for 8080`、`this is a project for 8081`、`this is a project for 8082`之间重复，这个就是因为我们在调用服务时的负载均衡器默认选择的是轮询策略，也就是会对获取到`service-hello`服务的所有实例之后，会依次轮询该实例来作为我们当次调用的实例。

### 扩展

在使用客户端负载均衡时，我们也可以直接使用注解的方式，这时候我们需要进行如下改造：

- EurekaConsumerApplication.java中新增一个Bean，代码如下：

```java
@Bean("loadBalanceRestTemplate")
@LoadBalanced
public RestTemplate loadBalanceRestTemplate() {
    return new RestTemplate();
}
```

- ConsumerController.java中我们新增代码

```java
// 注入新的ResTempalte
@Autowired
private RestTemplate loadBalanceRestTemplate;

// ---------------------------------省略代码，具体参考源码，此处只做分析----------------------------------------

// 增加如下方法
@GetMapping("serviceConsumer2")
public String serviceConsumer2() {
    return loadBalanceRestTemplate.getForObject("http://SERVICE-HELLO/getPort", String.class);
}
```

修改代码之后，我们访问`http://localhost:8083/serviceConsumer2`，我们会发现该`serviceConsumer2`与`serviceConsumer`两个方法返回时一致的，这个就是`Ribbon`的客户端负载均衡的两种实现，一个是`LoadBalancerClient`，一个是注解`@LoadBalanced`。

## 总结

以上就是`Ribbon`通过`RestTemplate`以及`spring-cloud-commons`包的基础服务消费，实现`commons`的接口，同时`Ribbon`提供了多种注册中心的实现，但是正具体的实现过程并不需要我们去了解，我们仅仅需要相同的调用方式就可以，这个也就为Spring Cloud在对各种组件选择上提供了很强的扩展性。

## 源码

- [eureka-consumer 源码参考](/eureka-consumer)