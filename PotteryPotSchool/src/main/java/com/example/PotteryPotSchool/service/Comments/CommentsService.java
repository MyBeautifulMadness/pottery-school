package com.example.PotteryPotSchool.service.Comments;

import com.example.PotteryPotSchool.dto.Comments.CommentCreateRequest;
import com.example.PotteryPotSchool.dto.Comments.CommentDetails;

import java.util.UUID;

public interface CommentsService {
    CommentDetails create(UUID postId, CommentCreateRequest request);
}
