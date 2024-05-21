package com.neusoft.neu24.common.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class MyGenerator {

    public static void main(String[] args) throws IOException {
        generate();
    }

    private static void generate() throws IOException {
        File directory = new File("");

//            **********************必须修改的数据库连接参数**************
        String dataUrl = "jdbc:mysql://localhost:3306/nep?serverTimezone=GMT%2b8";
        String userName = "root";
        String password = "030713";

        FastAutoGenerator.create(dataUrl, userName, password)
                .globalConfig(builder -> {
                    builder.author("Team-NEU-NanHu") // 设置作者
//                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("D:\\Work\\neusoft_nep\\emp-entity\\src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.neusoft.neu24") // 设置父包名
                            .moduleName(null) // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "D:\\Work\\neusoft_nep\\emp-entity\\src\\main\\resources\\com\\neusoft\\neu24\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.entityBuilder().enableLombok();
//                    builder.mapperBuilder().enableMapperAnnotation().build();
                    builder.controllerBuilder().enableHyphenStyle()  // 开启驼峰转连字符
                            .enableRestStyle();  // 开启生成@RestController 控制器

//                    **********************必须修改的表名**************
                    builder.addInclude("user");
                    builder.addInclude("aqi");
                    builder.addInclude("grid_city");
                    builder.addInclude("grid_province");
                    builder.addInclude("grid_town");
                    builder.addInclude("report");
                    builder.addInclude("statistics");
//                            .addTablePrefix("t_", "sys_"); // 设置过滤表前缀
                })
//                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}