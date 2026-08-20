package com.oj.api.service;

import com.oj.api.config.AppProperties;
import com.oj.common.messaging.JudgeJobMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class JudgeJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    public JudgeJobPublisher(RabbitTemplate rabbitTemplate, AppProperties appProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.appProperties = appProperties;
    }

    public void publish(JudgeJobMessage message) {
        String queue = message.isPriority() || message.getWarRoomId() != null
                ? appProperties.getQueues().getWar()
                : appProperties.getQueues().getPractice();
        rabbitTemplate.convertAndSend(queue, message);
    }
}
