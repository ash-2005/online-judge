package com.oj.api.service;

import com.oj.api.dto.ApiDtos.LeaderboardEntryDto;
import com.oj.api.repository.SubmissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {

    private final SubmissionRepository submissionRepository;

    public LeaderboardService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> leaderboard(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return submissionRepository.findLeaderboard(safeLimit).stream()
                .map(row -> new LeaderboardEntryDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }
}
