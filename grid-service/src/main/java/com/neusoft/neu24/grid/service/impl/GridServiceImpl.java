package com.neusoft.neu24.grid.service.impl;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.grid.mapper.GridMapper;
import com.neusoft.neu24.grid.service.IGridService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GridServiceImpl implements IGridService {

    @Resource
    private GridMapper gridMapper;

    @Resource
    private GridCacheService gridCacheService;

    /**
     * 获取网格层次信息
     * @return 网格层次树
     */
    public HttpResponseEntity<Map<String, Object>> getGridTree() {
        List<Grid> provinces = gridCacheService.getProvinces();
        List<Grid> cities = new ArrayList<>();
        List<Grid> towns = new ArrayList<>();

        // 查询所有省对应的市
        for ( Grid province : provinces ) {
            cities.addAll(gridCacheService.getCitiesByProvinceCode(province.getProvinceCode()));
        }

        // 查询所有市对应的区/县
        for ( Grid city : cities ) {
            towns.addAll(gridCacheService.getTownsByCityCode(city.getCityCode()));
        }

        // 创建省-市-区/县的映射
        Map<String, List<Grid>> provinceCityMap = cities.stream().collect(Collectors.groupingBy(Grid::getProvinceCode));
        Map<String, List<Grid>> cityTownMap = towns.stream().collect(Collectors.groupingBy(Grid::getCityCode));

        // 构建树形结构
        List<Map<String, Object>> provinceList = new ArrayList<>();
        for ( Grid province : provinces ) {
            Map<String, Object> provinceMap = new HashMap<>();
            provinceMap.put("provinceId", province.getProvinceId());
            provinceMap.put("provinceName", province.getProvinceName());
            provinceMap.put("provinceCode", province.getProvinceCode());

            List<Map<String, Object>> cityList = new ArrayList<>();
            for ( Grid city : provinceCityMap.getOrDefault(province.getProvinceCode(), Collections.emptyList()) ) {
                Map<String, Object> cityMap = new HashMap<>();
                cityMap.put("cityId", city.getCityId());
                cityMap.put("cityName", city.getCityName());
                cityMap.put("cityCode", city.getCityCode());

                List<Map<String, Object>> townList = new ArrayList<>();
                for ( Grid town : cityTownMap.getOrDefault(city.getCityCode(), Collections.emptyList()) ) {
                    Map<String, Object> townMap = new HashMap<>();
                    townMap.put("townId", town.getTownId());
                    townMap.put("townName", town.getTownName());
                    townMap.put("townCode", town.getTownCode());
                    townList.add(townMap);
                }
                cityMap.put("towns", townList);
                cityList.add(cityMap);
            }
            provinceMap.put("cities", cityList);
            provinceList.add(provinceMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("provinces", provinceList);
        return new HttpResponseEntity<Map<String, Object>>().success(result);
    }

    /**
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateGridTown(Grid grid) {
        if ( gridMapper.updateGridTown(grid) ) {
            gridCacheService.clearCache();
            return new HttpResponseEntity<Boolean>().success(true);
        }
        return HttpResponseEntity.UPDATE_FAIL;
    }

}
