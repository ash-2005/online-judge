package com.oj.api.repository;

import com.oj.common.entity.ProblemCompanyTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemCompanyTagRepository extends JpaRepository<ProblemCompanyTag, Long> {

    List<ProblemCompanyTag> findByProblemId(Long problemId);

    Optional<ProblemCompanyTag> findByProblemIdAndUserIdAndCompanyIgnoreCase(Long problemId, Long userId, String company);

    @Query("SELECT DISTINCT LOWER(t.company) FROM ProblemCompanyTag t ORDER BY LOWER(t.company)")
    List<String> findDistinctCompanies();

    long countByProblemIdAndCompanyIgnoreCase(Long problemId, String company);

    @Query("""
            SELECT t.problem.id FROM ProblemCompanyTag t
            WHERE LOWER(t.company) = LOWER(:company)
            GROUP BY t.problem.id
            """)
    List<Long> findProblemIdsByCompany(@Param("company") String company);
}
