package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.AddCompanyTagRequest;
import com.oj.api.dto.ApiDtos.CreateDiscussionRequest;
import com.oj.api.dto.ApiDtos.DiscussionDto;
import com.oj.api.dto.ApiDtos.ProblemDetailDto;
import com.oj.api.dto.ApiDtos.ProblemSummaryDto;
import com.oj.api.service.CompanyTagService;
import com.oj.api.service.DiscussionService;
import com.oj.api.service.ProblemService;
import com.oj.common.enums.Difficulty;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;
    private final DiscussionService discussionService;
    private final CompanyTagService companyTagService;

    public ProblemController(
            ProblemService problemService,
            DiscussionService discussionService,
            CompanyTagService companyTagService
    ) {
        this.problemService = problemService;
        this.discussionService = discussionService;
        this.companyTagService = companyTagService;
    }

    @GetMapping
    public ResponseEntity<Page<ProblemSummaryDto>> list(
            @RequestParam(name = "difficulty", required = false) Difficulty difficulty,
            @RequestParam(name = "tag", required = false) String tag,
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(problemService.list(difficulty, tag, company, q, page, size));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ProblemSummaryDto>> recent() {
        return ResponseEntity.ok(problemService.recent());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<ProblemDetailDto> get(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(problemService.getByIdOrSlug(idOrSlug));
    }

    @GetMapping("/{id}/discussions")
    public ResponseEntity<List<DiscussionDto>> listDiscussions(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.listByProblem(id));
    }

    @PostMapping("/{id}/discussions")
    public ResponseEntity<DiscussionDto> createDiscussion(
            @PathVariable Long id,
            @Valid @RequestBody CreateDiscussionRequest request
    ) {
        return ResponseEntity.ok(discussionService.create(id, request));
    }

    @PostMapping("/{id}/company-tags")
    public ResponseEntity<ProblemSummaryDto> addCompanyTag(
            @PathVariable Long id,
            @Valid @RequestBody AddCompanyTagRequest request
    ) {
        return ResponseEntity.ok(companyTagService.addTag(id, request));
    }
}
