package com.neusoft.neu24.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;

import java.util.List;

public interface IAqiService extends IService<Aqi> {

    HttpResponseEntity<List<Aqi>> getAllApiInfo();
}
