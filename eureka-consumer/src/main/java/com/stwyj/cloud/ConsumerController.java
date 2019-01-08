package com.stwyj.cloud;

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
