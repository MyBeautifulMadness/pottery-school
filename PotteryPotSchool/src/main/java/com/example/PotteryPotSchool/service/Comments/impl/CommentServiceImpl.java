package com.example.PotteryPotSchool.service.Comments.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Comments.CommentCreateRequest;
import com.example.PotteryPotSchool.dto.Comments.CommentDetails;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Comments.CommentEntity;
import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CommentRepository;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.ProfileRepository;
import com.example.PotteryPotSchool.service.Comments.CommentsService;
import com.example.PotteryPotSchool.service.Me.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentsService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MeService meService;
    private final ProfileRepository profileRepository;

    @Override
    public CommentDetails create(UUID postId, CommentCreateRequest request) {
        User currentUser = meService.getMe();

        validateRequest(request);

        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .authorId(currentUser.getId())
                .body(request.getBody())
                .createdAt(LocalDateTime.now())
                .build();

        CommentEntity savedComment = commentRepository.save(comment);
        return mapToDetails(savedComment);
    }

    @Override
    public List<CommentDetails> getByPostId(UUID postId) {
        meService.getMe();

        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        return commentRepository.findAllByPostId(postId).stream()
                .map(this::mapToDetails)
                .toList();
    }

    @Override
    public void delete(UUID commentId) {
        User currentUser = meService.getMe();

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден: " + commentId));

        boolean isTeacher = currentUser.getRole() == Role.TEACHER;
        boolean isAuthor = currentUser.getId().equals(comment.getAuthorId());

        if (!isTeacher && !isAuthor) {
            throw new ForbiddenException("Вы не можете удалить данный комментарий, его могут удалить только автор и учитель");
        }

        commentRepository.delete(comment);
    }


    private void validateRequest(CommentCreateRequest request) {
        if (request == null || request.getBody() == null || request.getBody().isBlank()) {
            throw new BadRequestException("Сообщение не должно быть пустым");
        }
    }

    private CommentDetails mapToDetails(CommentEntity comment) {
        ProfileEntity profile = profileRepository.findById(comment.getAuthorId()).orElseThrow(() -> new NotFoundException("Имя автора не найдено: " + comment.getAuthorId()));
        CommentDetails details = new CommentDetails();
        details.setId(comment.getId());
        details.setPostId(comment.getPostId());
        details.setAuthorId(comment.getAuthorId());
        details.setAuthorName(profile.getFullName());
        details.setBody(comment.getBody());
        details.setCreatedAt(comment.getCreatedAt());
        return details;
    }
}
