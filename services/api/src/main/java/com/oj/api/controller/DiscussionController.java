package com.oj.api.controller;

import com.oj.api.dto.ApiDtos.DiscussionDto;
import com.oj.api.service.DiscussionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<DiscussionDto> upvote(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.upvote(id));
    }
}
