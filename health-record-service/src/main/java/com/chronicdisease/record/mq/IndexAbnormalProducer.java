package com.chronicdisease.record.mq;

import com.alibaba.fastjson2.JSON;
import com.chronicdisease.record.config.RabbitMqConfig;
import com.chronicdisease.record.domain.message.IndexAbnormalMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 指标异常消息生产者
 */
@Slf4j
@Component
public class IndexAbnormalProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送指标异常通知消息
     */
    public void send(IndexAbnormalMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.exchangeName,
                    RabbitMqConfig.routingKey,
                    message);
            log.info("指标异常消息已发送: patientId={}, indexCode={}, label={}",
                    message.getPatientId(), message.getIndexCode(), message.getAbnormalLabel());
        } catch (Exception e) {
            // 发送失败不阻塞主流程
            log.error("指标异常消息发送失败: patientId={}, msg={}",
                    message.getPatientId(), JSON.toJSONString(message), e);
        }
    }
}
