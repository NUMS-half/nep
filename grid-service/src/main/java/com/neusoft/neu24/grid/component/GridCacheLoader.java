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
        Set<String> keys = redisTemplate.keys(GRID_KEY +"*");
        if ( !(keys == null || keys.isEmpty()) ) {
            return;
        }
        List<Grid> grids = gridMapper.selectGridByTownCode(null);
        Map<String, Object> map = grids.stream().collect(Collectors.toMap(Grid::redisTownCode, Function.identity()));
        redisTemplate.opsForValue().multiSet(map);
    }
}