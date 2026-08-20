package com.oj.api.service;

import com.oj.api.dto.ApiDtos.SolvedStatsDto;
import com.oj.api.dto.ApiDtos.SubmissionDto;
import com.oj.api.dto.ApiDtos.UpdateProfileRequest;
import com.oj.api.dto.AuthDtos.UserResponse;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.SubmissionRepository;
import com.oj.api.repository.UserRepository;
import com.oj.api.security.AuthSupport;
import com.oj.common.entity.User;
import com.oj.common.enums.Difficulty;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public UserService(UserRepository userRepository, SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return AuthService.toUserResponse(currentUserEntity());
    }

    @Transactional
    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = currentUserEntity();
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.dob() != null) {
            user.setDob(request.dob());
        }
        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new ApiException("Email already in use");
                }
            });
            user.setEmail(request.email());
        }
        return AuthService.toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> mySubmissions() {
        Long userId = AuthSupport.currentUserId();
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .map(SubmissionService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolvedStatsDto myStats() {
        Long userId = AuthSupport.currentUserId();
        Map<Difficulty, Long> byDifficulty = new EnumMap<>(Difficulty.class);
        for (Object[] row : submissionRepository.countAcceptedDistinctProblemsByDifficulty(userId)) {
            byDifficulty.put((Difficulty) row[0], ((Number) row[1]).longValue());
        }
        long easy = byDifficulty.getOrDefault(Difficulty.EASY, 0L);
        long medium = byDifficulty.getOrDefault(Difficulty.MEDIUM, 0L);
        long hard = byDifficulty.getOrDefault(Difficulty.HARD, 0L);
        return new SolvedStatsDto(easy, medium, hard, easy + medium + hard);
    }

    private User currentUserEntity() {
        return userRepository.findById(AuthSupport.currentUserId())
                .orElseThrow(() -> new ApiException("User not found"));
    }
}
