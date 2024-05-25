package com.neusoft.neu24.nepcommon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.neusoft.neu24.nepcommon.mapper") // 定义mybatis接口的扫描范围
public class NepCommonApplication {

	public static void main(String[] args) {
		SpringApplication.run(NepCommonApplication.class, args);
	}

}

