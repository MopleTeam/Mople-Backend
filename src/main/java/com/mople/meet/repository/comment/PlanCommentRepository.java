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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlanComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void increaseLikeCount(Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlanComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decreaseLikeCount(Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlanComment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :id")
    void increaseReplyCount(Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlanComment c SET c.replyCount = c.replyCount - 1 WHERE c.id = :id AND c.replyCount > 0")
    void decreaseReplyCount(Long id);
}
