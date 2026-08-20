package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.SolvedStatsDto;
import com.oj.api.dto.ApiDtos.SubmissionDto;
import com.oj.api.dto.ApiDtos.UpdateProfileRequest;
import com.oj.api.dto.AuthDtos.UserResponse;
import com.oj.api.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.me());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMe(request));
    }

    @GetMapping("/me/submissions")
    public ResponseEntity<List<SubmissionDto>> mySubmissions() {
        return ResponseEntity.ok(userService.mySubmissions());
    }

    @GetMapping("/me/stats")
    public ResponseEntity<SolvedStatsDto> myStats() {
        return ResponseEntity.ok(userService.myStats());
    }
}
