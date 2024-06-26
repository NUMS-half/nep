package com.neusoft.neu24.aqi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;

import java.util.List;
import java.util.Map;

public interface IAqiService extends IService<Aqi> {

    HttpResponseEntity<List<Aqi>> getAllApiInfo();

    HttpResponseEntity<Boolean> validateAqi(Statistics statistics);

//    HttpResponseEntity<Map<String, Object>> calculateAqi(Map<String, Object> map);
}
