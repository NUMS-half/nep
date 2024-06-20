package com.neusoft.neu24.statistics.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StatisticsMapperTest {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Test
    void selectStatisticsSummary() {
        System.out.println(statisticsMapper.selectStatisticsSummary().values());
    }
}