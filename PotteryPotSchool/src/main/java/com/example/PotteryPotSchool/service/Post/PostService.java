package com.example.PotteryPotSchool.service.Post;

import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;

public interface PostService {
    PostDetails createPost(PostCreateRequest request);
}
