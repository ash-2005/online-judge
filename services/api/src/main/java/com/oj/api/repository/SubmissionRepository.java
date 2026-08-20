package com.oj.api.repository;

import com.oj.common.entity.Submission;
import com.oj.common.enums.SubmissionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    boolean existsByUserIdAndStatus(Long userId, SubmissionStatus status);

    long countByStatus(SubmissionStatus status);

    @Query("""
            SELECT s.problem.difficulty, COUNT(DISTINCT s.problem.id)
            FROM Submission s
            WHERE s.user.id = :userId AND s.status = com.oj.common.enums.SubmissionStatus.ACCEPTED
            GROUP BY s.problem.difficulty
            """)
    List<Object[]> countAcceptedDistinctProblemsByDifficulty(@Param("userId") Long userId);

    @Query(value = """
            SELECT u.username,
                   COUNT(DISTINCT s.problem_id) AS accepted_count,
                   COUNT(DISTINCT CASE WHEN p.difficulty = 'EASY' THEN s.problem_id END) AS easy,
                   COUNT(DISTINCT CASE WHEN p.difficulty = 'MEDIUM' THEN s.problem_id END) AS medium,
                   COUNT(DISTINCT CASE WHEN p.difficulty = 'HARD' THEN s.problem_id END) AS hard
            FROM submissions s
            JOIN users u ON s.user_id = u.id
            JOIN problems p ON s.problem_id = p.id
            WHERE s.status = 'ACCEPTED'
            GROUP BY u.id, u.username
            ORDER BY accepted_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findLeaderboard(@Param("limit") int limit);
}
