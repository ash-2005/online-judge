package com.oj.api.service;

import com.oj.api.dto.ApiDtos.CreateDiscussionRequest;
import com.oj.api.dto.ApiDtos.DiscussionDto;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.DiscussionRepository;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.UserRepository;
import com.oj.api.security.AuthSupport;
import com.oj.common.entity.Discussion;
import com.oj.common.entity.Problem;
import com.oj.common.entity.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository
    ) {
        this.discussionRepository = discussionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DiscussionDto> listByProblem(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ApiException("Problem not found");
        }
        return discussionRepository.findByProblemIdOrderByCreatedAtDesc(problemId).stream()
                .map(DiscussionService::toDto)
                .toList();
    }

    @Transactional
    public DiscussionDto create(Long problemId, CreateDiscussionRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ApiException("Problem not found"));
        User user = userRepository.findById(AuthSupport.currentUserId())
                .orElseThrow(() -> new ApiException("User not found"));
        if (request.parentId() != null) {
            Discussion parent = discussionRepository.findById(request.parentId())
                    .orElseThrow(() -> new ApiException("Parent discussion not found"));
            if (!parent.getProblem().getId().equals(problemId)) {
                throw new ApiException("Parent discussion belongs to another problem");
            }
        }
        Discussion discussion = Discussion.builder()
                .problem(problem)
                .user(user)
                .parentId(request.parentId())
                .content(request.content())
                .upvotes(0)
                .build();
        return toDto(discussionRepository.save(discussion));
    }

    @Transactional
    public DiscussionDto upvote(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ApiException("Discussion not found"));
        discussion.setUpvotes(discussion.getUpvotes() + 1);
        return toDto(discussionRepository.save(discussion));
    }

    public static DiscussionDto toDto(Discussion discussion) {
        return new DiscussionDto(
                discussion.getId(),
                discussion.getProblem().getId(),
                discussion.getUser().getId(),
                discussion.getUser().getUsername(),
                discussion.getParentId(),
                discussion.getContent(),
                discussion.getUpvotes(),
                discussion.getCreatedAt()
        );
    }
}
