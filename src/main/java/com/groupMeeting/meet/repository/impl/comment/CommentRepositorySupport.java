package com.groupMeeting.meet.repository.impl.comment;

import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.meet.comment.QPlanComment;
import com.groupMeeting.entity.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanComment> findCommentFirstPage(Long postId, int size) {
        QPlanComment comment = QPlanComment.planComment;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(comment)
                .join(comment.writer, user).fetchJoin()
                .where(comment.postId.eq(postId), comment.parentId.isNull())
                .orderBy(comment.writeTime.desc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public List<PlanComment> findCommentNextPage(Long postId, Long cursorId, int size) {
        QPlanComment comment = QPlanComment.planComment;
        QUser user = QUser.user;

        LocalDateTime cursorWriteTime = queryFactory
                .select(comment.writeTime)
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchOne();

        return queryFactory
                .selectFrom(comment)
                .join(comment.writer, user).fetchJoin()
                .where(
                        comment.postId.eq(postId)
                                .and(comment.parentId.isNull())
                                .and(
                                        comment.writeTime.lt(cursorWriteTime)
                                                .or(comment.writeTime.eq(cursorWriteTime)
                                                        .and(comment.id.lt(cursorId)))
                                )
                )
                .orderBy(comment.writeTime.desc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public List<PlanComment> findCommentReplyFirstPage(Long postId, Long commentId, int size) {
        QPlanComment comment = QPlanComment.planComment;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(comment)
                .join(comment.writer, user).fetchJoin()
                .where(comment.postId.eq(postId), comment.parentId.eq(commentId))
                .orderBy(comment.writeTime.asc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public List<PlanComment> findCommentReplyNextPage(Long postId, Long commentId, Long cursorId, int size) {
        QPlanComment comment = QPlanComment.planComment;
        QUser user = QUser.user;

        LocalDateTime cursorWriteTime = queryFactory
                .select(comment.writeTime)
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchOne();

        return queryFactory
                .selectFrom(comment)
                .join(comment.writer, user).fetchJoin()
                .where(
                        comment.postId.eq(postId)
                                .and(comment.parentId.eq(commentId))
                                .and(
                                        comment.writeTime.lt(cursorWriteTime)
                                                .or(comment.writeTime.eq(cursorWriteTime)
                                                        .and(comment.id.lt(cursorId)))
                                )
                )
                .orderBy(comment.writeTime.asc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public Integer countComment(Long postId) {
        QPlanComment comment = QPlanComment.planComment;

        Long result = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.postId.eq(postId))
                .fetchOne();

        return result == null ? 0 : result.intValue();
    }

    public boolean validCursor(Long cursorId) {
        QPlanComment comment = QPlanComment.planComment;

        return queryFactory
                .selectOne()
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
