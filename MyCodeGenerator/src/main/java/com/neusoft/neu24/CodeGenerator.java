package com.neusoft.neu24;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

public class CodeGenerator {
    private static String driver = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/nep?characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true";
    private static String username = "root";
    private static String password = "123456";

    public static void main(String[] args) {
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("NEU-NanHu") // 作者
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java") // 输出目录
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("com.neusoft.neu24.xxx.xxx") // 设置根包名
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .xml("mapper")
                )
                .strategyConfig(builder -> builder
                        .addInclude("user") // 设置要生成的表名
                        .entityBuilder()
                        .enableLombok() // 启用Lombok
                        .enableTableFieldAnnotation() // 启用字段注解
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
