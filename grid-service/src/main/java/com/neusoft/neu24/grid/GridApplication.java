package com.neusoft.neu24.grid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.neusoft.neu24.client")
@ComponentScan(basePackages = {"com.neusoft.neu24"})
@SpringBootApplication
@EnableCaching
public class GridApplication {
    public static void main(String[] args) {
        SpringApplication.run(GridApplication.class, args);
    }
}