package com.chronicdisease.record.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // 指标异常通知 - 交换机
    public static final String exchangeName = "index-abnormal-exchange";
    // 指标异常通知 - 队列
    public static final String queueName = "index-abnormal-queue";
    // 指标异常通知 - 路由键
    public static final String routingKey = "index.abnormal.notify";

    @Bean
    public Exchange exchange(){
        return new DirectExchange(exchangeName,true,false,null);
    }

    @Bean
    public Queue queue(){
        return new Queue(queueName,true,false,false);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(exchange()).with(routingKey).noargs();
    }

    /**
     * 使用 JSON 序列化替代默认的 Java 序列化，避免 SecurityException
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
