package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    void deleteByCommentIdIn(List<Long> commentIds);

    void deleteByCommentId(Long commentId);

    List<CommentMention> findCommentMentionByCommentId(Long commentId);

    @Query("select cm.userId from CommentMention cm where cm.commentId = :commentId")
    List<Long> findUserIdByCommentId(Long commentId);
}
