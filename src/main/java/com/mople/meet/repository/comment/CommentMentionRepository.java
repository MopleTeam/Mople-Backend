package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from CommentMention cm " +
            "      where cm.commentId = :commentId "
    )
    void deleteByCommentId(Long commentId);

    List<CommentMention> findCommentMentionByCommentId(Long commentId);

    @Query("select cm.userId from CommentMention cm where cm.commentId = :commentId")
    List<Long> findUserIdByCommentId(Long commentId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from CommentMention cm " +
            "      where cm.commentId in :commentIds "
    )
    void deleteAllByCommentIdIn(List<Long> commentIds);
}
