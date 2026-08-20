package com.oj.api.service;

import com.oj.api.dto.ApiDtos.AddCompanyTagRequest;
import com.oj.api.dto.ApiDtos.CompanyDto;
import com.oj.api.dto.ApiDtos.ProblemSummaryDto;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.ProblemCompanyTagRepository;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.UserRepository;
import com.oj.api.security.AuthSupport;
import com.oj.common.entity.CompanyTagCount;
import com.oj.common.entity.Problem;
import com.oj.common.entity.ProblemCompanyTag;
import com.oj.common.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyTagService {

    private final ProblemCompanyTagRepository tagRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public CompanyTagService(
            ProblemCompanyTagRepository tagRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository
    ) {
        this.tagRepository = tagRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProblemSummaryDto addTag(Long problemId, AddCompanyTagRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ApiException("Problem not found"));
        User user = userRepository.findById(AuthSupport.currentUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        String company = request.company().trim();
        Optional<ProblemCompanyTag> existing = tagRepository
                .findByProblemIdAndUserIdAndCompanyIgnoreCase(problemId, user.getId(), company);
        if (existing.isPresent()) {
            throw new ApiException("You already tagged this company for the problem");
        }

        tagRepository.save(ProblemCompanyTag.builder()
                .problem(problem)
                .user(user)
                .company(company)
                .round(request.round())
                .build());

        refreshDenormalizedCounts(problem, company);
        return ProblemService.toSummary(problemRepository.save(problem));
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> listCompanies() {
        return tagRepository.findDistinctCompanies().stream()
                .map(name -> new CompanyDto(name, tagRepository.findProblemIdsByCompany(name).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProblemSummaryDto> problemsByCompany(String name) {
        List<Long> ids = tagRepository.findProblemIdsByCompany(name);
        if (ids.isEmpty()) {
            return List.of();
        }
        return problemRepository.findAllById(ids).stream()
                .map(ProblemService::toSummary)
                .toList();
    }

    private void refreshDenormalizedCounts(Problem problem, String company) {
        long count = tagRepository.countByProblemIdAndCompanyIgnoreCase(problem.getId(), company);
        List<CompanyTagCount> tags = problem.getCompanyTags() == null
                ? new ArrayList<>()
                : new ArrayList<>(problem.getCompanyTags());

        boolean updated = false;
        for (CompanyTagCount tag : tags) {
            if (tag.getCompany().equalsIgnoreCase(company)) {
                tag.setCount((int) count);
                updated = true;
                break;
            }
        }
        if (!updated) {
            tags.add(CompanyTagCount.builder().company(company).count((int) count).build());
        }
        problem.setCompanyTags(tags);
    }
}
