package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.service.Post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пост (преподаватель)")
    public PostDetails createPost(@RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Удалить пост (преподаватель)")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Список постов (лента)")
    public Paged<PostShortDetails> getPosts(
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getPosts(type, page, size);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Просмотр поста")
    public PostDetails getPostById(@PathVariable UUID postId) {
        return postService.getById(postId);
    }

    @PatchMapping("/{postId}")
    @Operation(summary = "Редактировать пост (преподаватель)")
    public PostDetails updatePost(@PathVariable UUID postId,
                                  @RequestBody PostUpdateRequest request) {
        return postService.update(postId, request);
    }
}
