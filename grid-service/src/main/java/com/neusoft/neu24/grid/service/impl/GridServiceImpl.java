package com.neusoft.neu24.grid.service.impl;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.grid.mapper.GridMapper;
import com.neusoft.neu24.grid.service.IGridService;
import com.neusoft.neu24.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.neusoft.neu24.config.RedisConstants.GRID_KEY;

@Service
public class GridServiceImpl implements IGridService {

    @Resource
    private GridMapper gridMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 根据区县编码获取完整的网格信息
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
//        try {
            // 1. 在缓存中查找
            if ( redisUtil.hasKey(redisKey) ) {
                return new HttpResponseEntity<Grid>().success((Grid) redisUtil.get(redisKey));
            }
            // 2. 在数据库中查找
            List<Grid> grids = gridMapper.selectGridByTownCode(townCode);
            if ( grids.isEmpty() ) {
                return new HttpResponseEntity<Grid>().resultIsNull(null);
            } else {
                Grid grid = grids.get(0);
                redisUtil.set(redisKey, grid);
                return new HttpResponseEntity<Grid>().success(grid);
            }
//        } catch ( Exception e ) {
//            return new HttpResponseEntity<Grid>().serverError(null);
//        }

    }

    /**
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateGridTown(Grid grid) {
        if ( gridMapper.updateGridTown(grid) ) {
            // 删除缓存
            redisUtil.delete(GRID_KEY + grid.getTownCode());
            return new HttpResponseEntity<Boolean>().success(true);
        }
        return HttpResponseEntity.UPDATE_FAIL;
    }

}
