package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.PlanComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {

    List<PlanComment> findAllByParentId(Long parentId);

    void deleteByIdIn(List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("update PlanComment c set c.deleted = true, c.deletedAt = now(), c.deletedBy = :userId where c.id = :commentId")
    int softDelete(Long commentId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update PlanComment c set c.deleted = true, c.deletedAt = now(), c.deletedBy = :userId where c.id in :commentIds")
    int softDeleteAll(List<Long> commentIds, Long userId);

    @Query("select c.id from PlanComment c where c.postId = :postId")
    List<Long> findIdsByPostId(Long postId);
}
