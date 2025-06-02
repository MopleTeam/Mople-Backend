package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.CommentReport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    @Query("select c from CommentReport c")
    List<CommentReport> allCommentReport();
}
