package com.neusoft.neu24.mapper;

import com.neusoft.neu24.entity.GridProvince;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GridProvinceMapperTest {

    @Autowired
    GridProvinceMapper gridProvinceMapper;

    @Test
    void insert() {
        String[] provinces = new String[]{"北京市","天津市","上海市","重庆市","河北省","山西省","台湾省","辽宁省","吉林省","黑龙江省","江苏省","浙江省","安徽省","福建省","江西省","山东省","河南省","湖北省","湖南省","广东省","甘肃省","四川省","贵州省","海南省","云南省","青海省","陕西省","广西壮族自治区","西藏自治区","宁夏回族自治区","新疆维吾尔自治区","内蒙古自治区","澳门特别行政区","香港特别行政区"};
        for ( String p : provinces ) {
            GridProvince gridProvince = new GridProvince();
            gridProvince.setProvinceName(p);
            gridProvinceMapper.insert(gridProvince);
        }
    }
}