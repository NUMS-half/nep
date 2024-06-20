package com.neusoft.neu24.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.statistics.mapper.AqiMapper;
import com.neusoft.neu24.statistics.service.IAqiService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.neusoft.neu24.config.RedisConstants.AQI_KEY;

@Service
public class AqiServiceImpl extends ServiceImpl<AqiMapper, Aqi> implements IAqiService {

    @Resource
    private RedisTemplate<String, Aqi> redisTemplate;

    @Override
    public HttpResponseEntity<List<Aqi>> getAllApiInfo() {
        List<Aqi> aqiList;
        aqiList = redisTemplate.opsForList().range(AQI_KEY, 0, -1);

        if ( aqiList == null || aqiList.isEmpty() ) {
            aqiList = baseMapper.selectList(null);
            if ( aqiList == null || aqiList.isEmpty() ) {
                return new HttpResponseEntity<List<Aqi>>().resultIsNull(null);
            }
            redisTemplate.opsForList().rightPushAll(AQI_KEY, aqiList);
        }
        return new HttpResponseEntity<List<Aqi>>().success(aqiList);

    }
}
