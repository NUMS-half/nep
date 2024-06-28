package com.neusoft.neu24.grid.service;


import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IGridService {

    /**
     * 根据区县编码获取完整的网格信息
     * @param townCode 区县编码
     * @return 完整的网格信息
     */
    HttpResponseEntity<Grid> selectGridByTownCode(String townCode);

    HttpResponseEntity<List<Grid>> selectGridByMultipleTownCodes(List<String> townCodes);

    HttpResponseEntity<Map<Object,Object>> selectProvinceMap();

    HttpResponseEntity<Map<Object,Object>> selectCitiesMapByProvince(String provinceCode);

    HttpResponseEntity<Map<Object,Object>> selectGridTotal();

    /**
     * 更新网格区/县信息
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    HttpResponseEntity<Boolean> updateGridTown(Grid grid);
}

