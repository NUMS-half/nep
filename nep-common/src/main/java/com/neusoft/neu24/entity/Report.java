package com.neusoft.neu24.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 *
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Data
public class Report implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公众监督员上报信息UUID
     */
    @TableId(value = "report_id")
    private String reportId;

    /**
     * 公众监督员ID
     */
    private String userId;

    /**
     * 上报信息所属网格：省份编码
     */
    private String provinceCode;

    /**
     * 上报信息所属网格：市编码
     */
    private String cityCode;

    /**
     * 上报信息所属网格：区/县编码
     */
    private String townCode;

    /**
     * 上报信息的详细地址
     */
    private String address;

    /**
     * 上报信息的内容描述
     */
    private String information;

    /**
     * 上报者预估AQI所属等级（1-6级）
     */
    private Integer estimatedLevel;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 指派网格员ID
     */
    private String gmUserId;

    /**
     * 指派时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignTime;

    /**
     * 上报信息当前状态：	0：未指派	1：已指派	2：已确认
     */
    private Integer state;


}
