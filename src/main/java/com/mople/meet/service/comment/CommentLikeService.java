package com.mople.meet.service.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.entity.meet.comment.CommentLike;
import com.mople.entity.meet.comment.CommentStats;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.comment.CommentLikeRepository;
import com.mople.meet.repository.comment.CommentStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final CommentStatsRepository statsRepository;

    @Transactional
    public boolean toggleLike(Long userId, PlanComment comment) {
        CommentStats stats = statsRepository.findById(comment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_COMMENT_STATS));

        Optional<CommentLike> existingLike = likeRepository.findByUserIdAndCommentId(userId, comment.getId());

        if (existingLike.isPresent() && stats.canDecreaseLikeCount()) {

            statsRepository.decreaseLikeCount(comment.getId());
            likeRepository.delete(existingLike.get());
            return false;
        }

        statsRepository.increaseLikeCount(comment.getId());
        likeRepository.insertIfNotExists(comment.getId(), userId);

        return true;
    }

    public List<Long> findLikedCommentIds(Long userId, List<Long> commentIds) {
        return likeRepository.findLikedCommentIds(userId, commentIds);
    }

    public boolean likedByMe(Long userId, Long commentId) {
        return likeRepository.existsByUserIdAndCommentId(userId, commentId);
    }
}
