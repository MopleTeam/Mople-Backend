package com.mople.meet.repository.impl.comment;

import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.meet.comment.QMeetComment;
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

    public List<MeetComment> findCommentPage(Long postId, Long cursorId, int size) {
        QMeetComment comment = QMeetComment.meetComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.status.eq(Status.ACTIVE))
                .and(comment.targetId.eq(postId))
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

    public Integer countComments(Long postId) {
        QMeetComment comment = QMeetComment.meetComment;

        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.targetId.eq(postId),
                        comment.parentId.isNull()
                )
                .fetchOne();

        return Math.toIntExact(count != null ? count : 0L);
    }

    public List<MeetComment> findCommentReplyPage(Long postId, Long commentId, Long cursorId, int size) {
        QMeetComment comment = QMeetComment.meetComment;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(comment.status.eq(Status.ACTIVE))
                .and(comment.targetId.eq(postId))
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

    public Integer countComment(Long postId) {
        QMeetComment comment = QMeetComment.meetComment;

        Long result = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.status.eq(Status.ACTIVE),
                        comment.targetId.eq(postId)
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
