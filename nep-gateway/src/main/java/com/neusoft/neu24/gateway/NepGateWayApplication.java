package com.neusoft.neu24.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.neusoft.neu24"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NepGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NepGateWayApplication.class, args);
    }
}
