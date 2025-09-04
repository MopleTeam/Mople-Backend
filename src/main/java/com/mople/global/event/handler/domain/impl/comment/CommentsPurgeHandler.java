package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentPurgeEvent;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.PlanCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CommentsPurgeHandler implements DomainEventHandler<CommentPurgeEvent> {

    private final PlanCommentRepository commentRepository;

    @Override
    public Class<CommentPurgeEvent> getHandledType() {
        return CommentPurgeEvent.class;
    }

    @Override
    public void handle(CommentPurgeEvent event) {
        List<Status> commentStatus = commentRepository.findStatusByIdIn(event.getCommentIds());

        boolean deleted = commentStatus.stream().allMatch(s -> Objects.equals(s, Status.DELETED));
        if (!deleted) {
            return;
        }

        commentRepository.deleteByIdIn(event.getCommentIds());
    }
}
