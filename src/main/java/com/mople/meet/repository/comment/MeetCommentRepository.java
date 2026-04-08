package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.MeetComment;
import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetCommentRepository extends JpaRepository<MeetComment, Long> {

    @Modifying(flushAutomatically = true)
    @Query(
            "update MeetComment c " +
            "   set c.status = :status, " +
            "       c.deletedAt = :deletedAt, " +
            "       c.deletedBy = :userId " +
            " where c.id in :commentIds " +
            "   and c.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> commentIds, Long userId, LocalDateTime deletedAt);

    @Query("select c.id from MeetComment c where c.parentId = :parentId and c.status = com.mople.global.enums.Status.ACTIVE")
    List<Long> findChildIds(Long parentId);

    @Query("select c.id from MeetComment c where c.postId = :postId and c.status = com.mople.global.enums.Status.ACTIVE")
    List<Long> findIdByPostId(Long postId);

    @Query("select c from MeetComment c where c.id = :id and c.status = :status")
    Optional<MeetComment> findByIdAndStatus(Long id, Status status);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete " +
            "  from MeetComment c " +
            " where c.id in :commentIds " +
            "   and c.status = com.mople.global.enums.Status.DELETED"
    )
    void hardDeleteById(List<Long> commentIds);

    @Query(value = "select version from plan_comment where comment_id = :commentId", nativeQuery = true)
    long findVersion(Long commentId);
}
