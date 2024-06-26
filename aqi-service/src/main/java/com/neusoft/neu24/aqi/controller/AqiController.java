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

@Slf4j
@RestController
@RequestMapping("/aqi")
public class AqiController {

    private static final Logger logger = LoggerFactory.getLogger(AqiController.class);

    @Resource
    private IAqiService aqiService;

    @GetMapping("/select/all")
    public HttpResponseEntity<List<Aqi>> selectAllAqi() {
        try {
            return aqiService.getAllApiInfo();
        } catch ( QueryException e ) {
            logger.error("获取所有AQI信息时发生异常", e);
            return new HttpResponseEntity<List<Aqi>>().serverError(null);
        }
    }

    @PostMapping("/validate")
    public HttpResponseEntity<Boolean> validateAqi(@RequestBody Statistics statistics) {
        try {
            return aqiService.validateAqi(statistics);
        } catch ( QueryException e ) {
            logger.error("校验AQI信息时发生异常", e);
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }

//    @PostMapping("/calculate")
//    public HttpResponseEntity<Map<String,Object>> calculateAqi(@RequestBody Map<String,Object> map) {
//        return aqiService.calculateAqi(map);
//    }
}
