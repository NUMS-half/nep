package com.neusoft.neu24.statistics.service.impl;

import com.neusoft.neu24.statistics.service.IAqiService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AqiServiceImplTest {

    @Resource
    IAqiService aqiService;

    @Test
    void calculateAqi() {
        aqiService.calculateAqi(Map.of("so2", 34.0, "co", 4.0, "spm", 200.0));
    }
}