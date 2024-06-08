package com.neusoft.neu24.chat.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.chat.mapper.MessageMapper;
import com.neusoft.neu24.chat.service.IChatService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Message;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.neusoft.neu24.config.RedisConstants.CHAT_HISTORY;


@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message> implements IChatService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
            // 1. 判断Redis中是否有该用户的历史消息
            String redisKey = CHAT_HISTORY + message.getUserId();
            boolean hasHistory = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));

            // 2.插入数据库
            if ( messageMapper.insert(message) > 0 ) {
                if ( hasHistory ) {
                    // 3. 如果已经存在历史记录，追加写入
                    redisTemplate.opsForList().rightPush(redisKey, message);
                    // 刷新过期时间
                    redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
                }
                return new HttpResponseEntity<Message>().success(message);
            } else {
                return new HttpResponseEntity<Message>().addFail(null);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Message>().addFail(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Message>().serverError(null);
        }
    }

    /**
     * 根据用户加载历史消息
     *
     * @param userId 用户ID
     * @return 历史消息列表
     */
    @Override
    public HttpResponseEntity<List<Message>> loadHistory(String userId) {
        try {
            // 用户ID不能为空
            if ( userId == null || userId.isEmpty() ) {
                return new HttpResponseEntity<List<Message>>().resultIsNull(null);
            }
            // 获取历史消息
            List<Message> messages;
            // 1.先从缓存中获取
            List<Object> cachedHistory = redisTemplate.opsForList().range(CHAT_HISTORY + userId, 0, -1);
            // 2.如果缓存中有数据，则转换后直接返回
            if ( cachedHistory != null && !cachedHistory.isEmpty() ) {
                messages = cachedHistory.stream().map(object -> {
                            Message message = new Message();
                            // 使用BeanUtil复制属性
                            BeanUtil.copyProperties(object, message);
                            return message;
                        })
                        .collect(Collectors.toList());
                return new HttpResponseEntity<List<Message>>().success(messages);
            }
            // 3.如果缓存中没有数据，则从数据库中获取
            messages = messageMapper.selectMessagesByUserId(userId);
            if ( messages != null && !messages.isEmpty() ) {
                // 4.将数据存入缓存
                messages.forEach(msg -> redisTemplate.opsForList().rightPush(CHAT_HISTORY + userId, msg));
                redisTemplate.expire(CHAT_HISTORY + userId, 30, TimeUnit.MINUTES);
                return new HttpResponseEntity<List<Message>>().success(messages);
            } else {
                return new HttpResponseEntity<List<Message>>().resultIsNull(null);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<Message>>().serverError(null);
        }
    }
}
