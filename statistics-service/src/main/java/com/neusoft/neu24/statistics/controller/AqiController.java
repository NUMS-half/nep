package com.neusoft.neu24.statistics.controller;

import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.statistics.service.IAqiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aqi")
public class AqiController {

    @Resource
    private IAqiService aqiService;

    @GetMapping("/select/all")
    public HttpResponseEntity<List<Aqi>> selectAllAqi() {
        return aqiService.getAllApiInfo();
    }

    @PostMapping("/calculate")
    public HttpResponseEntity<Map<String,Object>> calculateAqi(@RequestBody Map<String,Object> map) {
        return aqiService.calculateAqi(map);
    }
}
