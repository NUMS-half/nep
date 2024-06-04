package com.neusoft.neu24.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.statistics.mapper.StatisticsMapper;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {

    /**
     * <b>保存网格员测量的统计信息<b/>
     *
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    @Override
    public HttpResponseEntity<Statistics> saveStatistics(Statistics statistics) {
        return null;
    }

    /**
     * <b>更新统计信息<b/>
     *
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    @Override
    public HttpResponseEntity<Boolean> updateStatistics(Statistics statistics) {
        return null;
    }

    /**
     * <b>条件分页查询统计信息<b/>
     *
     * @param statistics 查询条件
     * @param current    当前页
     * @param size       每页数据条数
     * @return 分页查询结果
     */
    @Override
    public HttpResponseEntity<IPage<Statistics>> selectStatisticsByPage(Statistics statistics, long current, long size) {
        IPage<Statistics> page = new Page<>(current, size);
        IPage<Statistics> pages;
        QueryWrapper<Statistics> queryWrapper = new QueryWrapper<>();
        if(statistics != null){
            Map<String,Object> params = new HashMap<>();
            params.put("statistics_id",statistics.getStatisticsId());
            params.put("province_id",statistics.getProvinceId());
            params.put("city_id",statistics.getCityId());
            params.put("town_id",statistics.getTownId());
            params.put("address", statistics.getAddress());
            params.put("aqi_id", statistics.getAqiId());
            params.put("confirm_time", statistics.getConfirmTime());
            params.put("gm_user_id", statistics.getGmUserId());

            queryWrapper.allEq(params);
            pages = getBaseMapper().selectPage(page, queryWrapper);
        } else {
            pages = getBaseMapper().selectPage(page, null);
        }
        return pages == null || pages.getTotal() == 0 ?
                new HttpResponseEntity<IPage<Statistics>>().resultIsNull(null) :
                new HttpResponseEntity<IPage<Statistics>>().success(pages);
    }
}
