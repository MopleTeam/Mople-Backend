package com.mople.meet.service.comment;

import com.mople.entity.meet.comment.CommentLike;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.meet.repository.comment.CommentLikeRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final PlanCommentRepository commentRepository;

    @Transactional
    public boolean toggleLike(Long userId, PlanComment comment) {
        Optional<CommentLike> existingLike = likeRepository.findByUserIdAndCommentId(userId, comment.getId());

        if (existingLike.isPresent() && comment.canDecreaseLikeCount()) {
            commentRepository.decreaseLikeCount(comment.getId());
            likeRepository.delete(existingLike.get());
            return false;
        }

        commentRepository.increaseLikeCount(comment.getId());
        CommentLike like = CommentLike.builder()
                .userId(userId)
                .commentId(comment.getId())
                .build();
        likeRepository.save(like);

        return true;
    }

    public List<Long> findLikedCommentIds(Long userId, List<Long> commentIds) {
        return likeRepository.findLikedCommentIds(userId, commentIds);
    }

    public boolean likedByMe(Long userId, Long commentId) {
        return likeRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    public void deleteByCommentIds(List<Long> replyIds) {
        likeRepository.deleteByCommentIdIn(replyIds);
    }

    public void deleteByCommentId(Long commentId) {
        likeRepository.deleteByCommentId(commentId);
    }
}
