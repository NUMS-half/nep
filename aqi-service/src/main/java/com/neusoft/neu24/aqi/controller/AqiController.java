package com.neusoft.neu24.aqi.controller;

import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.aqi.service.IAqiService;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.exceptions.QueryException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <b>AQI前端控制器</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
@Slf4j
@RestController
@RequestMapping("/aqi")
public class AqiController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AqiController.class);

    /**
     * AQI服务接口
     */
    @Resource
    private IAqiService aqiService;

    /**
     * 获取所有AQI信息
     * @return 所有AQI信息
     */
    @GetMapping("/select/all")
    public HttpResponseEntity<List<Aqi>> selectAllAqi() {
        try {
            // 1. 调用服务接口获取所有AQI信息
            return aqiService.getAllApiInfo();
        } catch ( QueryException e ) {
            // 2. 异常处理
            logger.error("获取所有AQI信息时发生异常", e);
            return new HttpResponseEntity<List<Aqi>>().serverError(null);
        }
    }

    /**
     * 校验检测信息各项指标的合法性
     * @param statistics 检测信息
     * @return 是否合法
     */
    @PostMapping("/validate")
    public HttpResponseEntity<Boolean> validateAqi(@RequestBody Statistics statistics) {
        try {
            // 1. 调用服务接口校验AQI信息
            return aqiService.validateAqi(statistics);
        } catch ( QueryException e ) {
            // 2. 异常处理
            logger.error("校验AQI信息时发生异常", e);
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }

//    @PostMapping("/calculate")
//    public HttpResponseEntity<Map<String,Object>> calculateAqi(@RequestBody Map<String,Object> map) {
//        return aqiService.calculateAqi(map);
//    }
}
