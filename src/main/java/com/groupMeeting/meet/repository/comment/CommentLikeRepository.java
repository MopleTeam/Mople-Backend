package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("select cl.commentId from CommentLike cl where cl.userId = :userId and cl.commentId in :commentIds")
    List<Long> findLikedCommentIds(Long userId, List<Long> commentIds);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    void deleteByCommentIdIn(List<Long> commentIds);

    @Modifying
    void deleteByCommentId(Long commentId);
}
