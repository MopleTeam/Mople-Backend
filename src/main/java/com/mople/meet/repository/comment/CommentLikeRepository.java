package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("select cl.commentId from CommentLike cl where cl.userId = :userId and cl.commentId in :commentIds")
    List<Long> findLikedCommentIds(Long userId, List<Long> commentIds);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    @Query(value =
            "insert into comment_like (comment_id, user_id) " +
            "       values (:commentId, :userId) " +
            "       on conflict do nothing"
            , nativeQuery = true)
    void insertIfNotExists(Long commentId, Long userId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByCommentIdIn(List<Long> commentIds);

    void deleteByCommentId(Long commentId);
}
