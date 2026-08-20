package com.oj.api.service;

import com.oj.api.dto.ApiDtos.CreateSubmissionRequest;
import com.oj.api.dto.ApiDtos.SubmissionDto;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.SubmissionRepository;
import com.oj.api.repository.UserRepository;
import com.oj.api.repository.WarRoomParticipantRepository;
import com.oj.api.repository.WarRoomRepository;
import com.oj.api.security.AuthSupport;
import com.oj.common.entity.Problem;
import com.oj.common.entity.Submission;
import com.oj.common.entity.User;
import com.oj.common.entity.WarRoom;
import com.oj.common.enums.SubmissionStatus;
import com.oj.common.enums.WarRoomStatus;
import com.oj.common.messaging.JudgeJobMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final WarRoomRepository warRoomRepository;
    private final WarRoomParticipantRepository warRoomParticipantRepository;
    private final JudgeJobPublisher judgeJobPublisher;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository,
            WarRoomRepository warRoomRepository,
            WarRoomParticipantRepository warRoomParticipantRepository,
            JudgeJobPublisher judgeJobPublisher
    ) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.warRoomRepository = warRoomRepository;
        this.warRoomParticipantRepository = warRoomParticipantRepository;
        this.judgeJobPublisher = judgeJobPublisher;
    }

    @Transactional
    public SubmissionDto submit(CreateSubmissionRequest request) {
        Long userId = AuthSupport.currentUserId();
        if (submissionRepository.existsByUserIdAndStatus(userId, SubmissionStatus.PENDING)
                || submissionRepository.existsByUserIdAndStatus(userId, SubmissionStatus.RUNNING)) {
            throw new ApiException("You already have a pending submission");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new ApiException("Problem not found"));

        Long warRoomId = request.warRoomId();
        boolean priority = false;
        if (warRoomId != null) {
            WarRoom warRoom = warRoomRepository.findById(warRoomId)
                    .orElseThrow(() -> new ApiException("War room not found"));
            if (warRoom.getStatus() == WarRoomStatus.FINISHED) {
                throw new ApiException("War room already finished");
            }
            if (!warRoomParticipantRepository.existsByWarRoomIdAndUserId(warRoomId, userId)) {
                throw new ApiException("Join the war room before submitting");
            }
            if (!warRoom.getProblem().getId().equals(problem.getId())) {
                throw new ApiException("Problem does not match war room");
            }
            priority = true;
        }

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .warRoomId(warRoomId)
                .language(request.language())
                .code(request.code())
                .status(SubmissionStatus.PENDING)
                .build();
        submission = submissionRepository.save(submission);

        judgeJobPublisher.publish(JudgeJobMessage.builder()
                .submissionId(submission.getId())
                .priority(priority)
                .warRoomId(warRoomId)
                .build());

        return toDto(submission);
    }

    @Transactional(readOnly = true)
    public SubmissionDto getById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ApiException("Submission not found"));
        return toDto(submission);
    }

    public static SubmissionDto toDto(Submission submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getProblem().getTitle(),
                submission.getUser().getId(),
                submission.getUser().getUsername(),
                submission.getLanguage(),
                submission.getCode(),
                submission.getStatus(),
                submission.getRuntimeMs(),
                submission.getMemoryKb(),
                submission.getErrorMessage(),
                submission.getWarRoomId(),
                submission.getSubmittedAt(),
                submission.getJudgedAt()
        );
    }
}
