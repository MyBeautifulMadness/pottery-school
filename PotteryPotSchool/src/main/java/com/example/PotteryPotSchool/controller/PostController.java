package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.service.Post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetails createPost(@RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }
}
