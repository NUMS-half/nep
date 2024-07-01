package com.neusoft.neu24.aqi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.aqi.utils.AqiCalculator;
import com.neusoft.neu24.aqi.mapper.AqiMapper;
import com.neusoft.neu24.aqi.service.IAqiService;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.exceptions.QueryException;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.neusoft.neu24.config.RedisConstants.AQI_KEY;

@Slf4j
@Service
@Transactional
public class AqiServiceImpl extends ServiceImpl<AqiMapper, Aqi> implements IAqiService {

    private static final Logger logger = LoggerFactory.getLogger(AqiServiceImpl.class);

    @Resource
    private RedisTemplate<String, Aqi> redisTemplate;

    @Resource
    private AqiMapper aqiMapper;

    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Aqi>> getAllApiInfo() throws QueryException {
        try {
            List<Aqi> aqiList = getAqiList();
            if ( aqiList == null || aqiList.isEmpty() ) {
                return new HttpResponseEntity<List<Aqi>>().resultIsNull(null);
            }
            return new HttpResponseEntity<List<Aqi>>().success(aqiList);
        } catch ( Exception e ) {
            logger.error("获取所有AQI信息时发生异常", e);
            throw new QueryException("获取所有AQI信息时发生异常", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Boolean> validateAqi(Statistics statistics) throws QueryException {
        try {
            AqiCalculator calculator = new AqiCalculator(getAqiList());
            boolean resAqi = calculator.validateAqi(statistics);
            boolean resSo2 = calculator.validateItem(statistics.getSo2Level(), statistics.getSo2Value(), "so2");
            boolean resCo = calculator.validateItem(statistics.getCoLevel(), statistics.getCoValue(), "co");
            boolean resSpm = calculator.validateItem(statistics.getSpmLevel(), statistics.getSpmValue(), "spm");
            if ( resAqi && resSo2 && resCo && resSpm ) {
                logger.info("检测数值校验成功");
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("检测数值校验失败");
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATISTICS_VALUE_INVALID);
            }
        } catch ( Exception e ) {
            logger.error("校验AQI各项数值合法性时发生异常", e);
            throw new QueryException("校验AQI各项数值合法性时发生异常", e);
        }
    }

//    /**
//     * @param map
//     * @return
//     */
//    @Override
//    public HttpResponseEntity<Map<String, Object>> calculateAqi(Map<String, Object> map) {
//        double so2 = (double) map.get("so2");
//        double co = (double) map.get("co");
//        double spm = (double) map.get("spm");
//
//        List<Aqi> aqiList = getAqiList();
//
//        AqiCalculator calculator = new AqiCalculator(aqiList);
//
//        System.out.println("calculator.calculateAQI(\"so2\", so2) = " + calculator.calculateAQI("so2", so2));
//        System.out.println("calculator.calculateAQI(\"co\", co) = " + calculator.calculateAQI("co", co));
//        System.out.println("calculator.calculateAQI(\"spm\", spm) = " + calculator.calculateAQI("spm", spm));
//
//
//        return null;
//    }

    private List<Aqi> getAqiList() {
        List<Aqi> aqiList;
        try {
            aqiList = redisTemplate.opsForList().range(AQI_KEY, 0, -1);
            if ( aqiList == null || aqiList.isEmpty() ) {
                aqiList = aqiMapper.selectList(null);
                redisTemplate.opsForList().rightPushAll(AQI_KEY, aqiList);
            }
        } catch ( RedisCommandTimeoutException e ) {
            aqiList = aqiMapper.selectList(null);
        }
        return aqiList;
    }
}
