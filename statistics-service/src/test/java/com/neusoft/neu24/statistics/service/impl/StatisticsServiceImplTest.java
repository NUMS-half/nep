package com.neusoft.neu24.statistics.service.impl;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class StatisticsServiceImplTest {

    @Resource
    StatisticsServiceImpl statisticsService;

    @Test
    void selectItemizedStatistics() {
        System.out.println("Test selectItemizedStatistics");
        System.out.println(statisticsService.selectItemizedStatistics(null));
    }
}