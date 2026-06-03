package com.mople.global.event.handler.domain.impl.notice.delete;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.event.data.domain.notice.NoticeSoftDeletedEvent;
import com.mople.global.enums.CommentTarget;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.MeetCommentRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.mople.global.enums.Status.DELETED;
import static com.mople.global.enums.event.AggregateType.NOTICE;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class NoticeDeletedFanoutHandler implements DomainEventHandler<NoticeSoftDeletedEvent> {

    private final MeetCommentRepository commentRepository;
    private final OutboxService outboxService;

    @Override
    public Class<NoticeSoftDeletedEvent> getHandledType() {
        return NoticeSoftDeletedEvent.class;
    }

    @Override
    public void handle(NoticeSoftDeletedEvent event) {
        List<Long> commentIds = commentRepository.findIdByTargetAndTargetId(CommentTarget.NOTICE, event.noticeId());
        commentRepository.softDeleteAll(DELETED, commentIds, event.noticeDeletedBy(), LocalDateTime.now());

        chunk(commentIds, ids -> {
            CommentsSoftDeletedEvent deleteEvent = CommentsSoftDeletedEvent.builder()
                    .target(CommentTarget.NOTICE)
                    .targetId(event.noticeId())
                    .commentIds(ids)
                    .commentsDeletedBy(event.noticeDeletedBy())
                    .build();

            outboxService.save(COMMENTS_SOFT_DELETED, NOTICE, event.noticeId(), deleteEvent);
        });
    }
}
