package com.neusoft.neu24.grid.service.impl;

import com.neusoft.neu24.dto.StatisticsTotalDTO;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.grid.mapper.GridMapper;
import com.neusoft.neu24.grid.service.IGridService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.neusoft.neu24.config.RedisConstants.*;

@Service
public class GridServiceImpl implements IGridService {

    @Resource
    private GridMapper gridMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据 区县编码 获取完整的网格信息
     *
     * @param townCode 区县编码
     * @return 完整的网格信息
     */
    @Override
    public HttpResponseEntity<Grid> selectGridByTownCode(String townCode) {
        if ( townCode == null || townCode.isEmpty() ) {
            return new HttpResponseEntity<Grid>().resultIsNull(null);
        }
        String redisKey = GRID_KEY + townCode;
        try {
            // 1. 在缓存中查找
            if ( Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)) ) {
                return new HttpResponseEntity<Grid>().success((Grid) redisTemplate.opsForValue().get(redisKey));
            }
            // 2. 在数据库中查找
            List<Grid> grids = gridMapper.selectGridByTownCode(townCode);
            if ( grids.isEmpty() ) {
                return new HttpResponseEntity<Grid>().resultIsNull(null);
            } else {
                Grid grid = grids.get(0);
                redisTemplate.opsForValue().set(redisKey, grid);
                return new HttpResponseEntity<Grid>().success(grid);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<Grid>().serverError(null);
        }
    }

    /**
     * 获取省份编码和名称的Map映射
     * @return 省份编码和名称的Map映射
     */
    @Override
    public HttpResponseEntity<Map<Object, Object>> selectProvinceMap() {
        // 1. 在缓存中查找
        Map<Object, Object> cacheMap = redisTemplate.opsForHash().entries(PROVINCE_MAP_KEY);
        if ( cacheMap.isEmpty() ) {
            // 2. 缓存中没有时，在数据库中查找
            List<Map<Object, Object>> list = gridMapper.selectProvinceMap();
            Map<Object, Object> resultMap = list.stream()
                    .collect(Collectors.toMap(
                            map -> map.get("provinceCode"),
                            map -> map.get("provinceName")
                    ));
            if ( resultMap.isEmpty() ) {
                return new HttpResponseEntity<Map<Object, Object>>().resultIsNull(null);
            }
            redisTemplate.opsForHash().putAll(PROVINCE_MAP_KEY, resultMap);
            return new HttpResponseEntity<Map<Object, Object>>().success(resultMap);
        }
        return new HttpResponseEntity<Map<Object, Object>>().success(cacheMap);
    }

    /**
     * @param provinceCode
     * @return
     */
    @Override
    public HttpResponseEntity<Map<Object, Object>> selectCitiesMapByProvince(String provinceCode) {
        Map<Object, Object> cacheMap = redisTemplate.opsForHash().entries(CITY_MAP_KEY + provinceCode);
        if ( cacheMap.isEmpty() ) {
            List<Map<Object, Object>> list = gridMapper.selectCitiesMapByProvince(provinceCode);
            Map<Object, Object> resultMap = list.stream()
                    .collect(Collectors.toMap(
                            map -> map.get("cityCode"),
                            map -> map.get("cityName")
                    ));
            if ( resultMap.isEmpty() ) {
                return new HttpResponseEntity<Map<Object, Object>>().resultIsNull(null);
            }
            redisTemplate.opsForHash().putAll(CITY_MAP_KEY + provinceCode, resultMap);
            redisTemplate.expire(CITY_MAP_KEY + provinceCode, 30, TimeUnit.MINUTES);
            return new HttpResponseEntity<Map<Object, Object>>().success(resultMap);
        }
        return new HttpResponseEntity<Map<Object, Object>>().success(cacheMap);
    }

    /**
     * 获取省市区的网格总数
     * @return
     */
    @Override
    public HttpResponseEntity<Map<Object, Object>> selectGridTotal() {
        Map<Object, Object> cacheMap = redisTemplate.opsForHash().entries(GRID_TOTAL_KEY);
        if ( cacheMap.isEmpty() ) {
            StatisticsTotalDTO total = gridMapper.selectGridTotal();
            Map<Object, Object> resultMap = new HashMap<>();
            resultMap.put("province", total.getProvince());
            resultMap.put("city", total.getCity());
            resultMap.put("town", total.getTown());
            redisTemplate.opsForHash().putAll(GRID_TOTAL_KEY, resultMap);
            return new HttpResponseEntity<Map<Object, Object>>().success(resultMap);
        }
        return new HttpResponseEntity<Map<Object, Object>>().success(cacheMap);
    }

    /**
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateGridTown(Grid grid) {
        if ( gridMapper.updateGridTown(grid) ) {
            // 更新缓存
            redisTemplate.opsForValue().set(GRID_KEY + grid.getTownCode(), grid);
            return new HttpResponseEntity<Boolean>().success(true);
        }
        return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
    }

}
