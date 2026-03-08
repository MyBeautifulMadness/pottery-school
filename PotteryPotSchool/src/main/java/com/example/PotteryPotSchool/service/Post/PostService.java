package com.example.PotteryPotSchool.service.Post;

import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;

import java.util.UUID;

public interface PostService {
    PostDetails createPost(PostCreateRequest request);
    void delete(UUID postId);
}
