package com.oj.api.repository;

import com.oj.common.entity.Discussion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    List<Discussion> findByProblemIdOrderByCreatedAtDesc(Long problemId);
}
