package com.mople.meet.repository.comment;

import com.mople.entity.meet.comment.PlanComment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {
    @Query("select c from PlanComment c where c.postId = :postId order by c.writeTime desc")
    List<PlanComment> getComment(Long postId);

    List<PlanComment> findAllByParentId(Long parentId);

    void deleteByIdIn(List<Long> ids);

    @Modifying
    @Query("UPDATE PlanComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void increaseLikeCount(Long id);

    @Modifying
    @Query("UPDATE PlanComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id")
    void decreaseLikeCount(Long id);

    @Modifying
    @Query("UPDATE PlanComment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :id")
    void increaseReplyCount(Long id);

    @Modifying
    @Query("UPDATE PlanComment c SET c.replyCount = c.replyCount - 1 WHERE c.id = :id")
    void decreaseReplyCount(Long id);
}
