package com.oj.api.service;

import com.oj.api.dto.ApiDtos.StatsSummaryDto;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.SubmissionRepository;
import com.oj.api.repository.WarRoomRepository;
import com.oj.common.enums.WarRoomStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final WarRoomRepository warRoomRepository;

    public StatsService(
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            WarRoomRepository warRoomRepository
    ) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.warRoomRepository = warRoomRepository;
    }

    @Transactional(readOnly = true)
    public StatsSummaryDto summary() {
        long activeWarRooms = warRoomRepository.countByStatusIn(
                List.of(WarRoomStatus.WAITING, WarRoomStatus.IN_PROGRESS)
        );
        return new StatsSummaryDto(
                problemRepository.count(),
                submissionRepository.count(),
                activeWarRooms
        );
    }
}
