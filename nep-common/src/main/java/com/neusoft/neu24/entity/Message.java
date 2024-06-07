package com.neusoft.neu24.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Data

public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识UUID
     */
    private String messageId;

    /**
     * 消息所属用户ID
     */
    private String userId;

    /**
     * 消息接收方
     */
    private Integer recipient;

    /**
     * 消息发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 消息内容
     */
    private String content;
}
