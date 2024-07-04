package com.neusoft.neu24.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.neusoft.neu24.user.mapper") // 定义mybatis接口的扫描范围
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.neusoft.neu24"})
@EnableFeignClients(basePackages = "com.neusoft.neu24.client")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}

