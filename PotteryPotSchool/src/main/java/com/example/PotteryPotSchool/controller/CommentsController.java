package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Comments.CommentCreateRequest;
import com.example.PotteryPotSchool.dto.Comments.CommentDetails;
import com.example.PotteryPotSchool.service.Comments.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentsController {

    private final CommentsService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDetails createComment(@PathVariable UUID postId, @RequestBody CommentCreateRequest request) {
        return commentService.create(postId, request);
    }
}
