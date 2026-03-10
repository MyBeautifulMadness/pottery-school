package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Comments.CommentCreateRequest;
import com.example.PotteryPotSchool.dto.Comments.CommentDetails;
import com.example.PotteryPotSchool.service.Comments.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentsController {

    private final CommentsService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить комментарий к посту")
    public CommentDetails createComment(@PathVariable UUID postId, @RequestBody CommentCreateRequest request) {
        return commentService.create(postId, request);
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Комментарии к посту")
    public List<CommentDetails> getCommentsByPostId(@PathVariable UUID postId) {
        return commentService.getByPostId(postId);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Удалить комментарий (автор и преподаватель)")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
