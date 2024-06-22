package com.neusoft.neu24.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.statistics.utils.AqiCalculator;
import com.neusoft.neu24.statistics.mapper.AqiMapper;
import com.neusoft.neu24.statistics.service.IAqiService;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.neusoft.neu24.config.RedisConstants.AQI_KEY;

@Service
@Transactional
public class AqiServiceImpl extends ServiceImpl<AqiMapper, Aqi> implements IAqiService {

    @Resource
    private RedisTemplate<String, Aqi> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Aqi>> getAllApiInfo() {
        try {
            List<Aqi> aqiList = getAqiList();
            if ( aqiList == null || aqiList.isEmpty() ) {
                return new HttpResponseEntity<List<Aqi>>().resultIsNull(null);
            }
            return new HttpResponseEntity<List<Aqi>>().success(aqiList);
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<Aqi>>().serverError(null);
        }
    }

    /**
     * @param map
     * @return
     */
    @Override
    public HttpResponseEntity<Map<String, Object>> calculateAqi(Map<String, Object> map) {
        double so2 = (double) map.get("so2");
        double co = (double) map.get("co");
        double spm = (double) map.get("spm");

        List<Aqi> aqiList = getAqiList();

        AqiCalculator calculator = new AqiCalculator(aqiList);

        System.out.println("calculator.calculateAQI(\"so2\", so2) = " + calculator.calculateAQI("so2", so2));
        System.out.println("calculator.calculateAQI(\"co\", co) = " + calculator.calculateAQI("co", co));
        System.out.println("calculator.calculateAQI(\"spm\", spm) = " + calculator.calculateAQI("spm", spm));


        return null;
    }

    private List<Aqi> getAqiList() {
        List<Aqi> aqiList;
        try {
            aqiList = redisTemplate.opsForList().range(AQI_KEY, 0, -1);
            if ( aqiList == null || aqiList.isEmpty() ) {
                aqiList = baseMapper.selectList(null);
                redisTemplate.opsForList().rightPushAll(AQI_KEY, aqiList);
            }
        } catch ( RedisCommandTimeoutException e ) {
            aqiList = baseMapper.selectList(null);
        }
        return aqiList;
    }
}
