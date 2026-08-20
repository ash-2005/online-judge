package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.CompanyDto;
import com.oj.api.dto.ApiDtos.ProblemSummaryDto;
import com.oj.api.service.CompanyTagService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyTagService companyTagService;

    public CompanyController(CompanyTagService companyTagService) {
        this.companyTagService = companyTagService;
    }

    @GetMapping
    public ResponseEntity<List<CompanyDto>> list() {
        return ResponseEntity.ok(companyTagService.listCompanies());
    }

    @GetMapping("/{name}/problems")
    public ResponseEntity<List<ProblemSummaryDto>> problems(@PathVariable String name) {
        return ResponseEntity.ok(companyTagService.problemsByCompany(name));
    }
}
