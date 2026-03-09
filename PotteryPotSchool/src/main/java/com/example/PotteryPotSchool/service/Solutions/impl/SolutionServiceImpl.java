package com.example.PotteryPotSchool.service.Solutions.impl;

import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final PostRepository postRepository;

    @Override
    public List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            UserPrincipal user
    ) {

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teacher can view solutions");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getType() != PostType.TASK) {
            throw new NotFoundException("Solutions exist only for TASK posts");
        }

        List<SolutionSummaryDto> solutions;

        if (status == null) {
            solutions = solutionRepository.findByPostId(postId);
        } else {
            solutions = solutionRepository.findByPostIdAndStatus(postId, status);
        }

        return solutions;
    }
}
