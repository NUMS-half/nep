package com.neusoft.neu24.report.listener;

import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReportStateListener {

    @Resource
    private IReportService reportService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "statistics.save.queue", durable = "true"),
            exchange = @Exchange(name = "statistics.exchange", type = ExchangeTypes.TOPIC),
            key = {"save.success"}
    ))
    public void listenStatisticsSave(String reportId) {
        // 标记上报信息为已处理(state = 2)
        reportService.setReportState(reportId, 2);
    }

}
