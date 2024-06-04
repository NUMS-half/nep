package com.neusoft.neu24.statistics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.entity.Statistics;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IStatisticsService extends IService<Statistics> {


    /**
     * <b>保存网格员测量的统计信息<b/>
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    HttpResponseEntity<Statistics> saveStatistics(Statistics statistics);


    /**
     * <b>更新统计信息<b/>
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    HttpResponseEntity<Boolean> updateStatistics(Statistics statistics);

    /**
     * <b>条件分页查询统计信息<b/>
     * @param statistics 查询条件
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    HttpResponseEntity<IPage<Statistics>> selectStatisticsByPage(Statistics statistics, long current, long size);
}
