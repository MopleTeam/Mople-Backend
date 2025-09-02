package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.CommentLikeRepository;
import com.mople.meet.repository.comment.CommentMentionRepository;
import com.mople.meet.repository.comment.CommentStatsRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class CommentsCleanupHandler implements DomainEventHandler<CommentsSoftDeletedEvent> {

    private static final int CHUCK = 800;

    private final PlanCommentRepository commentRepository;
    private final CommentLikeRepository likeRepository;
    private final CommentMentionRepository mentionRepository;
    private final CommentStatsRepository statsRepository;

    @Override
    public Class<CommentsSoftDeletedEvent> getHandledType() {
        return CommentsSoftDeletedEvent.class;
    }

    @Override
    public void handle(CommentsSoftDeletedEvent event) {
        List<Long> commentIds = commentRepository.findIdsByPostId(event.getPostId());
        if (commentIds.isEmpty()) {
            return;
        }

        chunk(commentIds, CHUCK, ids -> {
            likeRepository.deleteAllByCommentIdIn(ids);
            mentionRepository.deleteAllByCommentIdIn(ids);
            statsRepository.deleteAllByCommentIdIn(ids);
        });
    }
}