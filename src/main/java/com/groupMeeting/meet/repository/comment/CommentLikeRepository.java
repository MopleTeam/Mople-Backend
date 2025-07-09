package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("select cl.comment.id from CommentLike cl where cl.user.id = :userId and cl.comment.id in :commentIds")
    List<Long> findLikedCommentIds(Long userId, List<Long> commentIds);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
}
