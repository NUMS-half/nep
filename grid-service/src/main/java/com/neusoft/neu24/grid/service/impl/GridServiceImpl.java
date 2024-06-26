package com.neusoft.neu24.grid.service.impl;

import com.neusoft.neu24.dto.StatisticsTotalDTO;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.grid.mapper.GridMapper;
import com.neusoft.neu24.grid.service.IGridService;
import io.lettuce.core.RedisException;
import io.seata.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.neusoft.neu24.config.RedisConstants.*;

@Slf4j
@Service
@Transactional
public class GridServiceImpl implements IGridService {

    private static final Logger logger = LoggerFactory.getLogger(GridServiceImpl.class);

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
    @Transactional(readOnly = true)
    public HttpResponseEntity<Grid> selectGridByTownCode(String townCode) throws QueryException {
        // 1. 校验输入是否为空
        if ( StringUtils.isEmpty(townCode) ) {
            return new HttpResponseEntity<Grid>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 获取 Redis 中的网格信息 Key
        String redisKey = GRID_KEY + townCode;
        try {
            // 3. 在缓存中查找
            if ( Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)) ) {
                return new HttpResponseEntity<Grid>().success((Grid) redisTemplate.opsForValue().get(redisKey));
            }
            // 4. 在数据库中查找
            List<Grid> grids = gridMapper.selectGridByTownCode(townCode);
            if ( grids.isEmpty() ) {
                // 数据库中没有该网格信息
                return new HttpResponseEntity<Grid>().resultIsNull(null);
            } else {
                Grid grid = grids.get(0);
                // 加入缓存
                redisTemplate.opsForValue().set(redisKey, grid);
                return new HttpResponseEntity<Grid>().success(grid);
            }
        } catch ( Exception e ) {
            logger.error("根据区/县编号: {} 查询网格发生异常: {}", townCode, e.getMessage(), e);
            throw new QueryException("根据区/县编号查询网格发生异常", e);
        }
    }

    /**
     * 获取省份编码和名称的Map映射
     *
     * @return 省份编码和名称的Map映射
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Map<Object, Object>> selectProvinceMap() throws QueryException {
        try {
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
        } catch ( RedisException e ) {
            logger.error("查询省份编码和名称的Map映射时，Redis数据库发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询省份编码和名称的Map映射时，Redis数据库发生异常", e);
        } catch ( Exception e ) {
            logger.error("查询省份编码和名称的Map映射发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询省份编码和名称的Map映射发生异常", e);
        }
    }

    /**
     * 根据省份编码获取城市编码和名称的Map映射
     *
     * @param provinceCode 省份编码
     * @return 城市编码和名称的Map映射
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Map<Object, Object>> selectCitiesMapByProvince(String provinceCode) throws QueryException {
        if ( StringUtils.isEmpty(provinceCode) ) {
            return new HttpResponseEntity<Map<Object, Object>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
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
        } catch ( Exception e ) {
            logger.error("根据省份编码查询城市编码和名称的Map映射发生异常: {}", e.getMessage());
            throw new QueryException("根据省份编码查询城市编码和名称的Map映射发生异常", e);
        }
    }

    /**
     * 获取省市区的网格总数
     *
     * @return 省市区的网格总数
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Map<Object, Object>> selectGridTotal() throws QueryException {
        try {
            Map<Object, Object> cacheMap = redisTemplate.opsForHash().entries(GRID_TOTAL_KEY);
            if ( cacheMap.isEmpty() ) {
                StatisticsTotalDTO total = gridMapper.selectGridTotal();
                if ( total == null ) {
                    return new HttpResponseEntity<Map<Object, Object>>().resultIsNull(null);
                }
                Map<Object, Object> resultMap = new HashMap<>();
                resultMap.put("province", total.getProvince());
                resultMap.put("city", total.getCity());
                resultMap.put("town", total.getTown());
                redisTemplate.opsForHash().putAll(GRID_TOTAL_KEY, resultMap);
                return new HttpResponseEntity<Map<Object, Object>>().success(resultMap);
            }
            return new HttpResponseEntity<Map<Object, Object>>().success(cacheMap);
        } catch ( RedisException e ) {
            logger.error("查询省市区的网格总数时，Redis数据库发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询省市区的网格总数时，Redis数据库发生异常", e);
        } catch ( Exception e ) {
            logger.error("查询省市区的网格总数发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询省市区的网格总数发生异常", e);
        }
    }

    /**
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<Boolean> updateGridTown(Grid grid) throws UpdateException {
        if ( grid == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            if ( gridMapper.updateGridTown(grid) ) {
                // 更新缓存
                redisTemplate.opsForValue().set(GRID_KEY + grid.getTownCode(), grid);
                return new HttpResponseEntity<Boolean>().success(true);
            }
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( RedisException e ) {
            logger.error("更新网格区/县信息时，Redis数据库发生异常: {}", e.getMessage(), e);
            throw new UpdateException("更新网格区/县信息时，Redis数据库发生异常", e);
        } catch ( Exception e ) {
            logger.error("更新网格区/县信息发生异常: {}", e.getMessage(), e);
            throw new UpdateException("更新网格区/县信息发生异常", e);
        }
    }

}
