package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentsPurgeEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.PlanCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentsPurgeHandler implements DomainEventHandler<CommentsPurgeEvent> {

    private final PlanCommentRepository commentRepository;

    @Override
    public Class<CommentsPurgeEvent> getHandledType() {
        return CommentsPurgeEvent.class;
    }

    @Override
    public void handle(CommentsPurgeEvent event) {
        commentRepository.hardDeleteById(event.commentIds());
    }
}
