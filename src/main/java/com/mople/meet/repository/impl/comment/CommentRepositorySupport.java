package com.mople.meet.repository.impl.comment;

import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.meet.comment.QMeetComment;
import com.mople.global.enums.CommentTarget;
import com.mople.global.enums.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<MeetComment> findCommentPage(CommentTarget target, Long targetId, Long cursorId, int size) {
        QMeetComment comment = QMeetComment.meetComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.status.eq(Status.ACTIVE))
                .and(comment.target.eq(target))
                .and(comment.targetId.eq(targetId))
                .and(comment.parentId.isNull());

        if (cursorId != null) {
            LocalDateTime cursorWriteTime = queryFactory
                    .select(comment.writeTime)
                    .from(comment)
                    .where(comment.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1} ) < ( {2}, {3} )",
                            comment.writeTime, comment.id, cursorWriteTime, cursorId
                    )
            );
        }

        return queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.writeTime.desc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public Integer countParentComments(CommentTarget target, Long targetId) {
        QMeetComment comment = QMeetComment.meetComment;

        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.target.eq(target),
                        comment.targetId.eq(targetId),
                        comment.parentId.isNull()
                )
                .fetchOne();

        return Math.toIntExact(count != null ? count : 0L);
    }

    public List<MeetComment> findCommentReplyPage(CommentTarget target, Long targetId, Long commentId, Long cursorId, int size) {
        QMeetComment comment = QMeetComment.meetComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.status.eq(Status.ACTIVE))
                .and(comment.target.eq(target))
                .and(comment.targetId.eq(targetId))
                .and(comment.parentId.eq(commentId));

        if (cursorId != null) {
            LocalDateTime cursorWriteTime = queryFactory
                    .select(comment.writeTime)
                    .from(comment)
                    .where(comment.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1} ) > ({2}, {3})",
                            comment.writeTime, comment.id, cursorWriteTime, cursorId
                    )
            );
        }

        return queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.writeTime.asc(), comment.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public Integer countTotalComment(CommentTarget target, Long targetId) {
        QMeetComment comment = QMeetComment.meetComment;

        Long result = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.target.eq(target),
                        comment.targetId.eq(targetId)
                )
                .fetchOne();

        return result == null ? 0 : result.intValue();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QMeetComment comment = QMeetComment.meetComment;

        return queryFactory
                .selectOne()
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.id.eq(cursorId)
                )
                .fetchFirst() == null;
    }
}
