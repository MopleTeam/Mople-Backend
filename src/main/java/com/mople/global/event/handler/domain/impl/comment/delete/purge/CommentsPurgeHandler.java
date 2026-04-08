package com.mople.global.event.handler.domain.impl.comment.delete.purge;

import com.mople.dto.event.data.domain.comment.CommentsPurgeEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.MeetCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentsPurgeHandler implements DomainEventHandler<CommentsPurgeEvent> {

    private final MeetCommentRepository commentRepository;

    @Override
    public Class<CommentsPurgeEvent> getHandledType() {
        return CommentsPurgeEvent.class;
    }

    @Override
    public void handle(CommentsPurgeEvent event) {
        commentRepository.hardDeleteById(event.commentIds());
    }
}
