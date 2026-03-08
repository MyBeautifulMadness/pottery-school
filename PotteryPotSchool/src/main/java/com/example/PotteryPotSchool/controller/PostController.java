package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Posts.Paged;
import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.dto.Posts.PostShortDetails;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.service.Post.PostService;
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
    public PostDetails createPost(@RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Paged<PostShortDetails> getPosts(
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getPosts(type, page, size);
    }

    @GetMapping("/{postId}")
    public PostDetails getPostById(@PathVariable UUID postId) {
        return postService.getById(postId);
    }
}
