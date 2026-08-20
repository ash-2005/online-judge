package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.WarRoomEvent;
import java.time.Instant;
import java.util.Map;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WarRoomWsController {

    @MessageMapping("/warroom/{id}/ping")
    @SendTo("/topic/warroom/{id}")
    public WarRoomEvent ping(@DestinationVariable Long id, Map<String, String> payload) {
        return new WarRoomEvent(
                "PING",
                id,
                null,
                null,
                payload != null ? payload.get("username") : null,
                "pong",
                Instant.now()
        );
    }
}
