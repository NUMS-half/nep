package com.neusoft.neu24.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.neusoft.neu24"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NepGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NepGateWayApplication.class, args);
    }
}
