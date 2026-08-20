package com.oj.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WarRoomRedisBridge implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(WarRoomRedisBridge.class);
    private static final String CHANNEL_PREFIX = "warroom:";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WarRoomRedisBridge(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (!channel.startsWith(CHANNEL_PREFIX)) {
            return;
        }
        String roomId = channel.substring(CHANNEL_PREFIX.length());
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            Map<String, Object> payload = objectMapper.readValue(body, new TypeReference<>() {
            });
            messagingTemplate.convertAndSend("/topic/warroom/" + roomId, payload);
        } catch (Exception e) {
            log.warn("Failed to bridge Redis war-room event on {}: {}", channel, e.getMessage());
        }
    }
}
