package com.oj.judge.service;

import com.oj.common.entity.Problem;
import com.oj.common.entity.Submission;
import com.oj.common.entity.TestCase;
import com.oj.common.entity.WarRoom;
import com.oj.common.enums.SubmissionStatus;
import com.oj.common.enums.WarRoomStatus;
import com.oj.common.messaging.JudgeJobMessage;
import com.oj.judge.repository.SubmissionRepository;
import com.oj.judge.repository.TestCaseRepository;
import com.oj.judge.repository.WarRoomRepository;
import com.oj.judge.sandbox.SandboxExecutor;
import com.oj.judge.sandbox.SandboxExecutor.PreparedJob;
import com.oj.judge.sandbox.SandboxResult;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final WarRoomRepository warRoomRepository;
    private final SandboxExecutor sandboxExecutor;
    private final StringRedisTemplate stringRedisTemplate;

    public JudgeService(
            SubmissionRepository submissionRepository,
            TestCaseRepository testCaseRepository,
            WarRoomRepository warRoomRepository,
            SandboxExecutor sandboxExecutor,
            StringRedisTemplate stringRedisTemplate) {
        this.submissionRepository = submissionRepository;
        this.testCaseRepository = testCaseRepository;
        this.warRoomRepository = warRoomRepository;
        this.sandboxExecutor = sandboxExecutor;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Transactional
    public void judge(JudgeJobMessage job) {
        if (job == null || job.getSubmissionId() == null) {
            log.warn("Ignoring judge job with null submissionId");
            return;
        }

        Submission submission = submissionRepository
                .findByIdWithDetails(job.getSubmissionId())
                .orElse(null);
        if (submission == null) {
            log.warn("Submission {} not found", job.getSubmissionId());
            return;
        }

        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setErrorMessage(null);
        submissionRepository.save(submission);

        Problem problem = submission.getProblem();
        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());
        if (testCases.isEmpty()) {
            fail(submission, SubmissionStatus.RUNTIME_ERROR, "No test cases for problem", 0);
            publishWarEvents(job, submission);
            return;
        }

        PreparedJob prepared = null;
        try {
            prepared = sandboxExecutor.prepare(submission.getLanguage(), submission.getCode());
            SandboxResult compileResult = sandboxExecutor.compile(prepared);
            if (compileResult.isTimedOut() || compileResult.isCompilationFailed()) {
                String err = truncate(compileResult.getStderr());
                if (err == null || err.isBlank()) {
                    err = compileResult.isTimedOut() ? "Compilation timed out" : "Compilation failed";
                }
                fail(submission, SubmissionStatus.COMPILATION_ERROR, err, (int) compileResult.getRuntimeMs());
                publishWarEvents(job, submission);
                return;
            }

            int maxRuntimeMs = 0;
            SubmissionStatus status = SubmissionStatus.ACCEPTED;
            String errorMessage = null;

            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                SandboxResult run = sandboxExecutor.run(
                        prepared,
                        tc.getInput() == null ? "" : tc.getInput(),
                        problem.getTimeLimitMs(),
                        problem.getMemoryLimitMb());

                maxRuntimeMs = Math.max(maxRuntimeMs, (int) run.getRuntimeMs());

                if (run.isTimedOut()) {
                    status = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    errorMessage = "Time limit exceeded on test case " + (i + 1);
                    break;
                }

                if (run.getExitCode() == null || run.getExitCode() != 0) {
                    status = SubmissionStatus.RUNTIME_ERROR;
                    String stderr = truncate(run.getStderr());
                    errorMessage = stderr == null || stderr.isBlank()
                            ? "Runtime error on test case " + (i + 1)
                            : stderr;
                    break;
                }

                if (!outputsMatch(run.getStdout(), tc.getExpectedOutput())) {
                    status = SubmissionStatus.WRONG_ANSWER;
                    errorMessage = "Wrong answer on test case " + (i + 1);
                    break;
                }
            }

            submission.setStatus(status);
            submission.setRuntimeMs(maxRuntimeMs);
            submission.setJudgedAt(Instant.now());
            submission.setErrorMessage(status == SubmissionStatus.ACCEPTED ? null : errorMessage);
            submissionRepository.save(submission);

            if (status == SubmissionStatus.ACCEPTED) {
                tryClaimWarWinner(job, submission);
            }
            publishWarEvents(job, submission);
        } catch (Exception e) {
            log.error("Judge failed for submission {}", job.getSubmissionId(), e);
            fail(submission, SubmissionStatus.RUNTIME_ERROR, truncate(e.getMessage()), 0);
            publishWarEvents(job, submission);
        } finally {
            sandboxExecutor.cleanup(prepared);
        }
    }

    private void tryClaimWarWinner(JudgeJobMessage job, Submission submission) {
        Long warRoomId = resolveWarRoomId(job, submission);
        if (warRoomId == null) {
            return;
        }

        WarRoom room = warRoomRepository.findById(warRoomId).orElse(null);
        if (room == null) {
            return;
        }

        if (room.getStatus() == WarRoomStatus.IN_PROGRESS && room.getWinnerId() == null) {
            Long userId = submission.getUser().getId();
            room.setWinnerId(userId);
            room.setStatus(WarRoomStatus.FINISHED);
            room.setEndedAt(Instant.now());
            warRoomRepository.save(room);

            String payload = "{\"type\":\"WINNER\",\"userId\":" + userId + "}";
            stringRedisTemplate.convertAndSend(warChannel(warRoomId), payload);
            log.info("War room {} winner set to user {}", warRoomId, userId);
        }
    }

    private void publishWarEvents(JudgeJobMessage job, Submission submission) {
        Long warRoomId = resolveWarRoomId(job, submission);
        if (warRoomId == null) {
            return;
        }
        Long userId = submission.getUser() != null ? submission.getUser().getId() : null;
        String payload = "{\"type\":\"SUBMISSION_UPDATE\",\"submissionId\":"
                + submission.getId()
                + ",\"status\":\""
                + submission.getStatus()
                + "\",\"userId\":"
                + userId
                + "}";
        stringRedisTemplate.convertAndSend(warChannel(warRoomId), payload);
    }

    private static Long resolveWarRoomId(JudgeJobMessage job, Submission submission) {
        if (job.getWarRoomId() != null) {
            return job.getWarRoomId();
        }
        return submission.getWarRoomId();
    }

    private static String warChannel(Long warRoomId) {
        return "warroom:" + warRoomId;
    }

    private void fail(Submission submission, SubmissionStatus status, String message, int runtimeMs) {
        submission.setStatus(status);
        submission.setErrorMessage(truncate(message));
        submission.setRuntimeMs(runtimeMs);
        submission.setJudgedAt(Instant.now());
        submissionRepository.save(submission);
    }

    static boolean outputsMatch(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 4000 ? value : value.substring(0, 4000);
    }
}
