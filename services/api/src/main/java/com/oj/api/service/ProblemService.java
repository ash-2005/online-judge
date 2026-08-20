package com.oj.api.service;

import com.oj.api.dto.ApiDtos.ProblemDetailDto;
import com.oj.api.dto.ApiDtos.ProblemSummaryDto;
import com.oj.api.dto.ApiDtos.SampleTestCaseDto;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.TestCaseRepository;
import com.oj.common.entity.Problem;
import com.oj.common.enums.Difficulty;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemService(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProblemSummaryDto> list(Difficulty difficulty, String tag, String company, String q, int page, int size) {
        String normalizedQ = blankToNull(q);
        String normalizedTag = blankToNull(tag);
        String normalizedCompany = blankToNull(company);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return problemRepository.findAll(buildSpec(difficulty, normalizedTag, normalizedCompany, normalizedQ), pageable)
                .map(ProblemService::toSummary);
    }

    private static Specification<Problem> buildSpec(
            Difficulty difficulty,
            String tag,
            String company,
            String q
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            if (tag != null) {
                Join<Object, Object> tags = root.join("tags");
                predicates.add(cb.equal(cb.lower(tags.as(String.class)), tag.toLowerCase(Locale.ROOT)));
                query.distinct(true);
            }
            if (company != null) {
                Join<Object, Object> companyTags = root.join("companyTags");
                predicates.add(cb.equal(
                        cb.lower(companyTags.get("company")),
                        company.toLowerCase(Locale.ROOT)
                ));
                query.distinct(true);
            }
            if (q != null) {
                String pattern = "%" + q.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)
                ));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional(readOnly = true)
    public ProblemDetailDto getByIdOrSlug(String idOrSlug) {
        Problem problem = resolve(idOrSlug);
        List<SampleTestCaseDto> samples = testCaseRepository.findByProblemIdAndIsSampleTrue(problem.getId()).stream()
                .map(tc -> new SampleTestCaseDto(tc.getInput(), tc.getExpectedOutput()))
                .toList();
        return toDetail(problem, samples);
    }

    @Transactional(readOnly = true)
    public List<ProblemSummaryDto> recent() {
        return problemRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(ProblemService::toSummary)
                .toList();
    }

    public Problem resolve(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return problemRepository.findById(Long.parseLong(idOrSlug))
                    .orElseThrow(() -> new ApiException("Problem not found"));
        }
        return problemRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new ApiException("Problem not found"));
    }

    public static ProblemSummaryDto toSummary(Problem problem) {
        return new ProblemSummaryDto(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDifficulty(),
                problem.getTags(),
                problem.getCompanyTags(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getCreatedAt()
        );
    }

    public static ProblemDetailDto toDetail(Problem problem, List<SampleTestCaseDto> samples) {
        return new ProblemDetailDto(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getStatement(),
                problem.getDifficulty(),
                problem.getTags(),
                problem.getCompanyTags(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getCreatedAt(),
                samples
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
