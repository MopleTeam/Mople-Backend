package com.groupMeeting.meet.repository.comment;

import com.groupMeeting.entity.meet.comment.PlanComment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {
    @Query("select c from PlanComment c where c.postId = :postId order by c.writeTime desc")
    List<PlanComment> getComment(Long postId);

    List<PlanComment> findAllByParentId(Long parentId);

    @Modifying
    void deleteByIdIn(List<Long> ids);
}
