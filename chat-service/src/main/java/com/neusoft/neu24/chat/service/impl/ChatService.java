package com.neusoft.neu24.chat.service.impl;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.chat.mapper.MessageMapper;
import com.neusoft.neu24.chat.service.IChatService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Message;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ChatService extends ServiceImpl<MessageMapper, Message> implements IChatService {

    @Resource
    private MessageMapper messageMapper;


    /**
     * 保存聊天消息
     *
     * @param message 待保存的消息对象
     * @return 是否保存成功
     */
    @Override
    public HttpResponseEntity<Message> saveChatMessage(Message message) {
        try {
            message.setSendTime(LocalDateTimeUtil.now());
            return messageMapper.insert(message) > 0 ?
                    new HttpResponseEntity<Message>().success(message) :
                    new HttpResponseEntity<Message>().addFail(null);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Message>().addFail(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Message>().serverError(null);
        }
    }

    /**
     * 根据用户加载历史消息
     * @param userId 用户ID
     * @return 历史消息列表
     */
    @Override
    public HttpResponseEntity<List<Message>> loadHistory(String userId) {
        try {
            if ( userId == null || userId.isEmpty() ) {
                return new HttpResponseEntity<List<Message>>().resultIsNull(null);
            }
            List<Message> messages = messageMapper.selectMessagesByUserId(userId);
            return messages != null ?
                    new HttpResponseEntity<List<Message>>().success(messages) :
                    new HttpResponseEntity<List<Message>>().resultIsNull(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<Message>>().serverError(null);
        }
    }
}
