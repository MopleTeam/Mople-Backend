package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    @Modifying
    void deleteByCommentIdIn(List<Long> commentIds);

    @Modifying
    void deleteByCommentId(Long commentId);

    List<CommentMention> findCommentMentionByCommentId(Long commentId);
}
