package com.oj.api.dto;

import com.oj.common.entity.CompanyTagCount;
import com.oj.common.enums.Difficulty;
import com.oj.common.enums.Language;
import com.oj.common.enums.SubmissionStatus;
import com.oj.common.enums.WarRoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record UpdateProfileRequest(String fullName, LocalDate dob, String email) {
    }

    public record SampleTestCaseDto(String input, String expectedOutput) {
    }

    public record ProblemSummaryDto(
            Long id,
            String title,
            String slug,
            Difficulty difficulty,
            List<String> tags,
            List<CompanyTagCount> companyTags,
            Integer timeLimitMs,
            Integer memoryLimitMb,
            Instant createdAt
    ) {
    }

    public record ProblemDetailDto(
            Long id,
            String title,
            String slug,
            String statement,
            Difficulty difficulty,
            List<String> tags,
            List<CompanyTagCount> companyTags,
            Integer timeLimitMs,
            Integer memoryLimitMb,
            Instant createdAt,
            List<SampleTestCaseDto> sampleTestCases
    ) {
    }

    public record CreateProblemRequest(
            @NotBlank String title,
            @NotBlank String slug,
            @NotBlank String statement,
            @NotNull Difficulty difficulty,
            List<String> tags,
            @NotNull Integer timeLimitMs,
            @NotNull Integer memoryLimitMb
    ) {
    }

    public record UpdateProblemRequest(
            String title,
            String slug,
            String statement,
            Difficulty difficulty,
            List<String> tags,
            Integer timeLimitMs,
            Integer memoryLimitMb
    ) {
    }

    public record CreateTestCaseRequest(
            @NotNull Long problemId,
            @NotBlank String input,
            @NotBlank String expectedOutput,
            boolean sample
    ) {
    }

    public record CreateSubmissionRequest(
            @NotNull Long problemId,
            @NotNull Language language,
            @NotBlank String code,
            Long warRoomId
    ) {
    }

    public record SubmissionDto(
            Long id,
            Long problemId,
            String problemTitle,
            Long userId,
            String username,
            Language language,
            String code,
            SubmissionStatus status,
            Integer runtimeMs,
            Integer memoryKb,
            String errorMessage,
            Long warRoomId,
            Instant submittedAt,
            Instant judgedAt
    ) {
    }

    public record CreateDiscussionRequest(
            @NotBlank String content,
            Long parentId
    ) {
    }

    public record DiscussionDto(
            Long id,
            Long problemId,
            Long userId,
            String username,
            Long parentId,
            String content,
            int upvotes,
            Instant createdAt
    ) {
    }

    public record AddCompanyTagRequest(
            @NotBlank String company,
            String round
    ) {
    }

    public record CompanyDto(String name, long problemCount) {
    }

    public record StatsSummaryDto(long problemCount, long submissionCount, long activeWarRooms) {
    }

    public record SolvedStatsDto(long easy, long medium, long hard, long totalAccepted) {
    }

    public record LeaderboardEntryDto(
            String username,
            long acceptedCount,
            long easy,
            long medium,
            long hard
    ) {
    }

    public record CreateWarRoomRequest(
            @NotNull Long problemId,
            @NotNull Integer maxParticipants
    ) {
    }

    public record WarRoomDto(
            Long id,
            String roomCode,
            Long problemId,
            String problemTitle,
            int maxParticipants,
            WarRoomStatus status,
            Long winnerId,
            Instant startedAt,
            Instant endedAt,
            Instant createdAt,
            long participantCount
    ) {
    }

    public record WarRoomEvent(
            String type,
            Long warRoomId,
            String roomCode,
            Long userId,
            String username,
            String message,
            Instant at
    ) {
    }
}
