package com.example.PotteryPotSchool.service.Post;

import com.example.PotteryPotSchool.dto.Posts.Paged;
import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.dto.Posts.PostShortDetails;
import com.example.PotteryPotSchool.enums.Posts.PostType;

import java.util.UUID;

public interface PostService {
    PostDetails createPost(PostCreateRequest request);
    void delete(UUID postId);
    Paged<PostShortDetails> getPosts(PostType type, int page, int size);
}
