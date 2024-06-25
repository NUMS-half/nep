package com.neusoft.neu24.aqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.neusoft.neu24.client")
@ComponentScan(basePackages = {"com.neusoft.neu24"})
public class AqiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AqiApplication.class, args);
    }
}
