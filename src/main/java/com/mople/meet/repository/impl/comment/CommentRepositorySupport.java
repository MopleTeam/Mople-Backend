package com.mople.meet.repository.impl.comment;

import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.meet.comment.QPlanComment;
import com.mople.global.enums.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanComment> findCommentPage(Long postId, Long cursorId, int size) {
        QPlanComment comment = QPlanComment.planComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.postId.eq(postId))
                .and(comment.parentId.isNull());

        if (cursorId != null) {
            LocalDateTime cursorWriteTime = queryFactory
                    .select(comment.writeTime)
                    .from(comment)
                    .where(comment.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(
                    comment.writeTime.lt(cursorWriteTime)
                            .or(comment.writeTime.eq(cursorWriteTime)
                                    .and(comment.id.lt(cursorId)))
            );
        }

        return queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.writeTime.desc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public Long countComments(Long postId) {
        QPlanComment comment = QPlanComment.planComment;

        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.postId.eq(postId),
                        comment.parentId.isNull()
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    public List<PlanComment> findCommentReplyPage(Long postId, Long commentId, Long cursorId, int size) {
        QPlanComment comment = QPlanComment.planComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.postId.eq(postId))
                .and(comment.parentId.eq(commentId));

        if (cursorId != null) {
            LocalDateTime cursorWriteTime = queryFactory
                    .select(comment.writeTime)
                    .from(comment)
                    .where(comment.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(comment.writeTime.gt(cursorWriteTime)
                    .or(comment.writeTime.eq(cursorWriteTime)
                            .and(comment.id.gt(cursorId)))
            );
        }

        return queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.writeTime.asc(), comment.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public Integer countComment(Long postId) {
        QPlanComment comment = QPlanComment.planComment;

        Long result = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.postId.eq(postId)
                )
                .fetchOne();

        return result == null ? 0 : result.intValue();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QPlanComment comment = QPlanComment.planComment;

        return queryFactory
                .selectOne()
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
