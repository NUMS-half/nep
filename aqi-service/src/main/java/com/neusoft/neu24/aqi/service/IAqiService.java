package com.neusoft.neu24.aqi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;

import java.util.List;

/**
 * <b>AQI服务接口</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IAqiService extends IService<Aqi> {

    /**
     * 获取所有AQI信息
     * @return 所有AQI信息
     */
    HttpResponseEntity<List<Aqi>> getAllApiInfo();

    /**
     * 校验检测信息各项指标的合法性
     * @param statistics 检测信息
     * @return 是否合法
     */
    HttpResponseEntity<Boolean> validateAqi(Statistics statistics);

    /**
     * 根据输入参数计算AQI
     * @param map 参数
     * @return AQI指数计算结果
     */
//    HttpResponseEntity<Map<String, Object>> calculateAqi(Map<String, Object> map);
}
