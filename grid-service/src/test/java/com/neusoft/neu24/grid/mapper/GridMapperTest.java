package com.neusoft.neu24.grid.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GridMapperTest {

    @Autowired
    private GridMapper gridMapper;

    @Test
    void selectGridByTownCode() {
        System.out.println(gridMapper.selectGridByTownCode("110101").get(0).toString());
    }

    @Test
    void selectProvinceMap() {
        System.out.println(gridMapper.selectProvinceMap());
    }
}