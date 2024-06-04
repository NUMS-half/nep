package com.neusoft.neu24.report.mapper;

import com.neusoft.neu24.entity.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Report类Mapper 接口
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    @Update("UPDATE report SET state = #{state} WHERE report.report_id = #{id}")
    int updateState(@Param("state") Integer state, @Param("id") String reportId);
}
