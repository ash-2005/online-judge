package com.oj.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.api.dto.ApiDtos.WarRoomEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WarRoomEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WarRoomEventPublisher(
            StringRedisTemplate redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(WarRoomEvent event) {
        messagingTemplate.convertAndSend("/topic/warroom/" + event.warRoomId(), event);
        try {
            String payload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend("warroom:" + event.warRoomId(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize war room event", e);
        }
    }
}
