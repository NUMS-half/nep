package com.neusoft.neu24.grid.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GridServiceImplTest {

    @Autowired
    GridServiceImpl gridService;

    @Test
    void selectCitiesMapByProvince() {
        System.out.println(gridService.selectCitiesMapByProvince("140000"));
    }
}