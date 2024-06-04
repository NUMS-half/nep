package com.neusoft.neu24.statistics.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.ReportClient;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.statistics.mapper.StatisticsMapper;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>统计信息服务实现类</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;

    private final ReportClient reportClient;

    /**
     * <b>保存网格员测量的统计信息<b/>
     *
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    @Override
    public HttpResponseEntity<Statistics> saveStatistics(Statistics statistics) {
        try {
            // 设置统计的确认时间
            statistics.setConfirmTime(LocalDateTimeUtil.now());
            if ( statisticsMapper.insert(statistics) != 0 ) {
                // 远程调用更新反馈的状态
                if ( Boolean.TRUE.equals(reportClient.setReportState(statistics.getReportId(), 2).getData()) ) {
                    return new HttpResponseEntity<Statistics>().success(statistics);
                } else {
                    return new HttpResponseEntity<Statistics>().serviceUnavailable(null);
                }
            } else {
                return new HttpResponseEntity<Statistics>().addFail(null);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Statistics>().addFail(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Statistics>().serverError(null);
        }
    }

    /**
     * <b>更新统计信息<b/>
     *
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    @Override
    public HttpResponseEntity<Boolean> updateStatistics(Statistics statistics) {
        try {
            return statisticsMapper.updateById(statistics) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
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
        if ( statistics != null ) {
            Map<String, Object> params = new HashMap<>();
            params.put("statistics_id", statistics.getStatisticsId());
            params.put("province_id", statistics.getProvinceId());
            params.put("city_id", statistics.getCityId());
            params.put("town_id", statistics.getTownId());
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

    /**
     * <b>根据ID查询统计信息<b/>
     *
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    @Override
    public HttpResponseEntity<Statistics> selectStatisticsById(String statisticsId) {
        if ( statisticsId == null ) {
            return new HttpResponseEntity<Statistics>().resultIsNull(null);
        } else {
            QueryWrapper<Statistics> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("statistics_id", statisticsId);
            Statistics result = statisticsMapper.selectOne(queryWrapper);
            if ( result == null ) {
                return new HttpResponseEntity<Statistics>().resultIsNull(null);
            } else {
                return new HttpResponseEntity<Statistics>().success(result);
            }
        }
    }

}
