package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.PlanComment;
import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {

    @Modifying(clearAutomatically = true)
    @Query(
            "update PlanComment c " +
            "   set c.status = :status, " +
            "       c.deletedAt = :deletedAt, " +
            "       c.deletedBy = :userId " +
            " where c.id in :commentIds " +
            "   and c.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> commentIds, Long userId, LocalDateTime deletedAt);

    void deleteByIdIn(List<Long> commentIds);

    @Query("select c.status from PlanComment c where c.id in :commentIds")
    List<Status> findStatusByIdIn(List<Long> commentIds);

    List<Long> findIdsByPostIdAndStatus(Long postId, Status status);
}
