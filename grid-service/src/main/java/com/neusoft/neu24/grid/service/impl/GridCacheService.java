package com.neusoft.neu24.grid.service.impl;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.grid.mapper.GridMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 网格Redis缓存服务类
 */
@Service
public class GridCacheService {

    @Resource
    private GridMapper gridMapper;

    @Cacheable(value = "provinces")
    public List<Grid> getProvinces() {
        return gridMapper.getProvinces();
    }

    @Cacheable(value = "cities", key = "#provinceCode")
    public List<Grid> getCitiesByProvinceCode(String provinceCode) {
        return gridMapper.getCitiesByProvinceCode(provinceCode);
    }

    @Cacheable(value = "towns", key = "#cityCode")
    public List<Grid> getTownsByCityCode(String cityCode) {
        return gridMapper.getTownsByCityCode(cityCode);
    }

    /**
     * 当数据库更新时，清除缓存
     */
    @CacheEvict(value = {"provinces", "cities", "towns"}, allEntries = true)
    public void clearCache() {
        // This method will clear all the cache entries
    }
}
