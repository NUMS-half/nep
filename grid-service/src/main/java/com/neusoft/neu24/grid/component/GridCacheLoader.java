package com.neusoft.neu24.grid.component;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.grid.mapper.GridMapper;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.neusoft.neu24.config.RedisConstants.GRID_KEY;


/**
 * 网格数据启动时缓存加载器
 *
 * @author wyx
 * @since 2024-05-21
 */
@Component
public class GridCacheLoader implements CommandLineRunner {

    @Resource
    private GridMapper gridMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 启动时，加载网格信息到缓存
     *
     * @param args 启动参数
     */
    @Override
    @Transactional
    public void run(String... args) {
        // 1. 如果缓存中已经有网格数据(Key存在)，不再加载
        Set<String> keys = redisTemplate.keys(GRID_KEY +"*");
        if ( !(keys == null || keys.isEmpty()) ) {
            return;
        }
        // 2. 否则，从数据库中查询网格信息，加载到缓存
        List<Grid> grids = gridMapper.selectGridByTownCode(null);
        Map<String, Object> map = grids.stream().collect(Collectors.toMap(Grid::redisTownCode, Function.identity()));
        redisTemplate.opsForValue().multiSet(map);
    }
}