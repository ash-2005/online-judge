package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.CreateSubmissionRequest;
import com.oj.api.dto.ApiDtos.SubmissionDto;
import com.oj.api.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionDto> submit(@Valid @RequestBody CreateSubmissionRequest request) {
        return ResponseEntity.ok(submissionService.submit(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getById(id));
    }
}
