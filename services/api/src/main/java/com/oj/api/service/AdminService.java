package com.oj.api.service;

import com.oj.api.dto.ApiDtos.CreateProblemRequest;
import com.oj.api.dto.ApiDtos.CreateTestCaseRequest;
import com.oj.api.dto.ApiDtos.ProblemDetailDto;
import com.oj.api.dto.ApiDtos.UpdateProblemRequest;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.TestCaseRepository;
import com.oj.common.entity.Problem;
import com.oj.common.entity.TestCase;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemService problemService;

    public AdminService(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            ProblemService problemService
    ) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.problemService = problemService;
    }

    @Transactional
    public ProblemDetailDto createProblem(CreateProblemRequest request) {
        if (problemRepository.findBySlug(request.slug()).isPresent()) {
            throw new ApiException("Slug already exists");
        }
        Problem problem = Problem.builder()
                .title(request.title())
                .slug(request.slug())
                .statement(request.statement())
                .difficulty(request.difficulty())
                .tags(request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>())
                .timeLimitMs(request.timeLimitMs())
                .memoryLimitMb(request.memoryLimitMb())
                .build();
        problem = problemRepository.save(problem);
        return problemService.getByIdOrSlug(String.valueOf(problem.getId()));
    }

    @Transactional
    public ProblemDetailDto updateProblem(Long id, UpdateProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Problem not found"));
        if (request.title() != null) {
            problem.setTitle(request.title());
        }
        if (request.slug() != null) {
            problemRepository.findBySlug(request.slug()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ApiException("Slug already exists");
                }
            });
            problem.setSlug(request.slug());
        }
        if (request.statement() != null) {
            problem.setStatement(request.statement());
        }
        if (request.difficulty() != null) {
            problem.setDifficulty(request.difficulty());
        }
        if (request.tags() != null) {
            problem.setTags(new ArrayList<>(request.tags()));
        }
        if (request.timeLimitMs() != null) {
            problem.setTimeLimitMs(request.timeLimitMs());
        }
        if (request.memoryLimitMb() != null) {
            problem.setMemoryLimitMb(request.memoryLimitMb());
        }
        problemRepository.save(problem);
        return problemService.getByIdOrSlug(String.valueOf(id));
    }

    @Transactional
    public Map<String, Object> createTestCase(CreateTestCaseRequest request) {
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new ApiException("Problem not found"));
        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(request.input())
                .expectedOutput(request.expectedOutput())
                .isSample(request.sample())
                .build();
        testCase = testCaseRepository.save(testCase);
        return Map.of(
                "id", testCase.getId(),
                "problemId", problem.getId(),
                "sample", testCase.isSample()
        );
    }
}
