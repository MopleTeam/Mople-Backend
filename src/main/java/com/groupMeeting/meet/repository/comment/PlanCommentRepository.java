package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.PlanComment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {
    @Query("select c from PlanComment c where c.postId = :postId order by c.writeTime desc")
    Slice<PlanComment> getComment(Long postId, Pageable pageable);
}
