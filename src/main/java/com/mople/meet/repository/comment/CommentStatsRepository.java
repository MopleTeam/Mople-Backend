package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.CommentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CommentStatsRepository extends JpaRepository<CommentStats, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentStats s SET s.likeCount = s.likeCount + 1 WHERE s.commentId = :commentId")
    void increaseLikeCount(Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentStats s SET s.likeCount = s.likeCount - 1 WHERE s.commentId = :commentId AND s.likeCount > 0")
    void decreaseLikeCount(Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentStats s SET s.replyCount = s.replyCount + 1 WHERE s.commentId = :commentId")
    void increaseReplyCount(Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentStats s SET s.replyCount = s.replyCount - 1 WHERE s.commentId = :commentId AND s.replyCount > 0")
    void decreaseReplyCount(Long commentId);

    void deleteAllByCommentIdIn(List<Long> commentIds);
}
