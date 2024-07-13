package com.neusoft.neu24.report.mapper;

import com.neusoft.neu24.entity.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <b>Report 类 Mapper 接口</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 设置反馈信息状态
     *
     * @param state    目标状态
     * @param reportId 反馈信息ID
     * @return 更新是否成功
     */
    @Update("UPDATE report SET state = #{state} WHERE report.report_id = #{id}")
    int updateState(@Param("state") Integer state, @Param("id") String reportId);
}
