package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.CreateWarRoomRequest;
import com.oj.api.dto.ApiDtos.WarRoomDto;
import com.oj.api.service.WarRoomService;
import com.oj.common.enums.WarRoomStatus;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warrooms")
public class WarRoomController {

    private final WarRoomService warRoomService;

    public WarRoomController(WarRoomService warRoomService) {
        this.warRoomService = warRoomService;
    }

    @PostMapping
    public ResponseEntity<WarRoomDto> create(@Valid @RequestBody CreateWarRoomRequest request) {
        return ResponseEntity.ok(warRoomService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<WarRoomDto>> list(
            @RequestParam(name = "status", required = false) WarRoomStatus status
    ) {
        return ResponseEntity.ok(warRoomService.list(status));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<WarRoomDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(warRoomService.getByCode(code));
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<WarRoomDto> join(@PathVariable String code) {
        return ResponseEntity.ok(warRoomService.join(code));
    }
}
