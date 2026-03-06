package com.example.PotteryPotSchool.service;

import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.entity.Users.User;

public interface PostService {
    PostDetails createPost(PostCreateRequest request);
}
