package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
}
