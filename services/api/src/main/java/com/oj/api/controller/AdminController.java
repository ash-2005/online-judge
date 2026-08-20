package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.CreateProblemRequest;
import com.oj.api.dto.ApiDtos.CreateTestCaseRequest;
import com.oj.api.dto.ApiDtos.ProblemDetailDto;
import com.oj.api.dto.ApiDtos.UpdateProblemRequest;
import com.oj.api.service.AdminService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/problems")
    public ResponseEntity<ProblemDetailDto> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        return ResponseEntity.ok(adminService.createProblem(request));
    }

    @PutMapping("/problems/{id}")
    public ResponseEntity<ProblemDetailDto> updateProblem(
            @PathVariable Long id,
            @RequestBody UpdateProblemRequest request
    ) {
        return ResponseEntity.ok(adminService.updateProblem(id, request));
    }

    @PostMapping("/testcases")
    public ResponseEntity<Map<String, Object>> createTestCase(@Valid @RequestBody CreateTestCaseRequest request) {
        return ResponseEntity.ok(adminService.createTestCase(request));
    }
}
