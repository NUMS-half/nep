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

import static com.neusoft.neu24.config.RedisConstants.AQI_KEY;


/**
 * <b>AQI服务实现类</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Service
@Transactional
public class AqiServiceImpl extends ServiceImpl<AqiMapper, Aqi> implements IAqiService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AqiServiceImpl.class);

    /**
     * Redis操作工具
     */
    @Resource
    private RedisTemplate<String, Aqi> redisTemplate;

    /**
     * AQI数据访问 Mapper
     */
    @Resource
    private AqiMapper aqiMapper;

    /**
     * 获取所有AQI信息
     * @return 所有AQI信息
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Aqi>> getAllApiInfo() throws QueryException {
        try {
            // 1. 获取所有AQI信息列表
            List<Aqi> aqiList = getAqiList();
            if ( aqiList == null || aqiList.isEmpty() ) {
                return new HttpResponseEntity<List<Aqi>>().resultIsNull(null);
            }
            // 2. 返回成功响应
            logger.info("获取所有AQI信息成功");
            return new HttpResponseEntity<List<Aqi>>().success(aqiList);
        } catch ( Exception e ) {
            // 3. 异常处理
            logger.error("获取所有AQI信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("获取所有AQI信息时发生异常", e);
        }
    }

    /**
     * 校验检测信息各项指标的合法性
     * @param statistics 检测信息
     * @return 是否合法
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Boolean> validateAqi(Statistics statistics) throws QueryException {
        try {
            // 1. 实例化 AQI 计算器工具
            AqiCalculator calculator = new AqiCalculator(getAqiList());
            // 2. 校验 AQI 各项数值合法性
            boolean resAqi = calculator.validateAqi(statistics);
            boolean resSo2 = calculator.validateItem(statistics.getSo2Level(), statistics.getSo2Value(), "so2");
            boolean resCo = calculator.validateItem(statistics.getCoLevel(), statistics.getCoValue(), "co");
            boolean resSpm = calculator.validateItem(statistics.getSpmLevel(), statistics.getSpmValue(), "spm");
            // 3. 返回校验结果
            if ( resAqi && resSo2 && resCo && resSpm ) {
                logger.info("AQI检测数值校验成功");
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("AQI检测数值校验失败");
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATISTICS_VALUE_INVALID);
            }
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("校验AQI各项数值合法性时发生异常: {}", e.getMessage(), e);
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

    /**
     * 获取所有AQI列表
     * @return AQI列表
     */
    private List<Aqi> getAqiList() {
        List<Aqi> aqiList;
        try {
            // 1. 先从 Redis 中获取 AQI 列表
            aqiList = redisTemplate.opsForList().range(AQI_KEY, 0, -1);
            if ( aqiList == null || aqiList.isEmpty() ) {
                // 2. 如果 Redis 中没有 AQI 列表，则从数据库中查询
                aqiList = aqiMapper.selectList(null);
                redisTemplate.opsForList().rightPushAll(AQI_KEY, aqiList);
            }
        } catch ( RedisCommandTimeoutException e ) {
            // 3. 如果 Redis 操作超时，则直接从数据库中查询
            aqiList = aqiMapper.selectList(null);
        }
        // 4. 返回 AQI 列表
        return aqiList;
    }
}
