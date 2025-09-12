package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.PlanComment;
import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {

    @Modifying(flushAutomatically = true)
    @Query(
            "update PlanComment c " +
            "   set c.status = :status, " +
            "       c.deletedAt = :deletedAt, " +
            "       c.deletedBy = :userId " +
            " where c.id = :commentId " +
            "   and c.version = :baseVersion" +
            "   and c.status <> :status"
    )
    int softDelete(Status status, Long commentId, Long userId, long baseVersion, LocalDateTime deletedAt);

    @Modifying(flushAutomatically = true)
    @Query(
            "update PlanComment c " +
            "   set c.status = :status, " +
            "       c.deletedAt = :deletedAt, " +
            "       c.deletedBy = :userId " +
            " where c.id in :commentIds " +
            "   and c.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> commentIds, Long userId, LocalDateTime deletedAt);

    @Query("select c.id from PlanComment c where c.parentId = :parentId and c.status = com.mople.global.enums.Status.ACTIVE")
    List<Long> findChildIds(Long parentId);

    @Query("select c.id from PlanComment c where c.postId = :postId and c.status = com.mople.global.enums.Status.ACTIVE")
    List<Long> findIdByPostId(Long postId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete " +
            "  from PlanComment c " +
            " where c.id in :commentIds " +
            "   and c.status = com.mople.global.enums.Status.DELETED"
    )
    void hardDeleteById(List<Long> commentIds);

    @Query("select c.version from PlanComment c where c.id = :commentId")
    long findVersion(Long commentId);
}
