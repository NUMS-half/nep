package com.neusoft.neu24.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Message;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IChatService extends IService<Message> {

    HttpResponseEntity<Message> saveChatMessage(Message message);

    /**
     * 根据用户加载历史消息
     * @param userId 用户ID
     * @return 历史消息列表
     */
    HttpResponseEntity<List<Message>> loadHistory(String userId);
}
