package com.example.PotteryPotSchool.service.Post;

import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.enums.Posts.PostType;

import java.util.UUID;

public interface PostService {
    PostDetails createPost(PostCreateRequest request);
    void delete(UUID postId);
    Paged<PostShortDetails> getPosts(PostType type, int page, int size);
    PostDetails getById(UUID postId);
    PostDetails update(UUID postId, PostUpdateRequest request);
}
