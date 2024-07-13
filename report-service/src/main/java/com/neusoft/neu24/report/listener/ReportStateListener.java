package com.neusoft.neu24.report.listener;

import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>反馈信息状态更新MQ监听类<b/>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Component
public class ReportStateListener {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ReportStateListener.class);

    /**
     * 反馈信息服务接口
     */
    @Resource
    private IReportService reportService;

    /**
     * 监听统计信息保存成功的消息，对上报信息进行状态更新
     *
     * @param reportId 上报信息ID
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "statistics.save.queue", durable = "true"),
            exchange = @Exchange(name = "statistics.exchange", type = ExchangeTypes.TOPIC),
            key = {"save.success"}
    ))
    @Transactional
    public void listenStatisticsSave(String reportId) {
        // 标记上报信息为已处理(state = 2)
        try {
            logger.info("接收到实测信息保存成功的消息，开始更新反馈信息: {} 状态", reportId);
            if ( reportService.setReportState(reportId, 2).getCode() == 200 ) {
                logger.info("更新反馈信息: {} 状态成功", reportId);
            } else {
                logger.error("更新反馈信息: {} 状态失败", reportId);
                throw new UpdateException("更新反馈信息状态失败");
            }
        } catch ( Exception e ) {
            logger.error("更新上报信息状态时发生异常: {}", e.getMessage());
            throw new UpdateException("更新上报信息状态时发生异常", e);
        }
    }

}
