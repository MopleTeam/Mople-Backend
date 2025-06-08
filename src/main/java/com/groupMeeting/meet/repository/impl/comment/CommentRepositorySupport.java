package com.groupMeeting.meet.repository.impl.comment;

import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.entity.meet.comment.QPlanComment;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<CommentResponse> findFirstPage(Long postId, int size) {
        QPlanComment comment = QPlanComment.planComment;

        return queryFactory
                .select(
                        Projections.constructor(
                                CommentResponse.class,
                                comment.id,
                                comment.postId,
                                comment.writerId,
                                comment.writerNickname,
                                comment.writerImg,
                                comment.content,
                                comment.writeTime
                ))
                .from(comment)
                .where(comment.postId.eq(postId))
                .orderBy(comment.writeTime.desc(), comment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public List<CommentResponse> findNextPage(Long postId, Long cursorId, int size) {
        QPlanComment comment = QPlanComment.planComment;

        LocalDateTime cursorWriteTime = queryFactory
                .select(comment.writeTime)
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchOne();

        return queryFactory
                .select(
                        Projections.constructor(
                                CommentResponse.class,
                                comment.id,
                                comment.postId,
                                comment.writerId,
                                comment.writerNickname,
                                comment.writerImg,
                                comment.content,
                                comment.writeTime
                ))
                .from(comment)
                .where(
                        comment.postId.eq(postId)
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

    public boolean isValidCursor(Long cursorId) {
        QPlanComment comment = QPlanComment.planComment;

        return queryFactory
                .select(comment.id)
                .from(comment)
                .where(comment.id.eq(cursorId))
                .limit(1)
                .fetchFirst() != null
                &&
                queryFactory.select(comment.writeTime)
                .from(comment)
                .where(comment.id.eq(cursorId))
                .fetchOne() != null;
    }
}
