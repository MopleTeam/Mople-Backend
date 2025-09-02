package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.CommentLikeRepository;
import com.mople.meet.repository.comment.CommentMentionRepository;
import com.mople.meet.repository.comment.CommentStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class CommentsCleanupHandler implements DomainEventHandler<CommentsSoftDeletedEvent> {

    private final CommentLikeRepository likeRepository;
    private final CommentMentionRepository mentionRepository;
    private final CommentStatsRepository statsRepository;

    @Override
    public Class<CommentsSoftDeletedEvent> getHandledType() {
        return CommentsSoftDeletedEvent.class;
    }

    @Override
    public void handle(CommentsSoftDeletedEvent event) {
        chunk(event.getCommentIds(), ids -> {
            likeRepository.deleteAllByCommentIdIn(ids);
            mentionRepository.deleteAllByCommentIdIn(ids);
            statsRepository.deleteAllByCommentIdIn(ids);
        });
    }
}