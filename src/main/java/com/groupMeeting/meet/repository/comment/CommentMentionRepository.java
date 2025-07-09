package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    void deleteByCommentId(Long commentId);

    List<CommentMention> findCommentMentionByCommentId(Long commentId);
}
