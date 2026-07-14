package com.mople.meet.service.comment;

import com.mople.core.exception.custom.ConcurrencyConflictException;
import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.request.meet.comment.CommentReportRequest;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.CommentReport;
import com.mople.entity.meet.comment.MeetComment;
import com.mople.global.enums.CommentTarget;
import com.mople.global.enums.Status;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentReportRepository;
import com.mople.meet.repository.comment.MeetCommentRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.outbox.service.OutboxService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final MeetPlanRepository planRepository;
    private final MeetCommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final EntityReader reader;
    private final CommentValidator commentValidator;

    private final PostCommentService postCommentService;
    private final OutboxService outboxService;

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        reader.findUser(userId);
        MeetComment comment = reader.findComment(commentId);

        CommentTarget type = comment.getTarget();
        Long targetId = comment.getTargetId();

        Meet meet = reader.findMeet(getMeetId(type, targetId));

        commentValidator.validateDeletionAuth(comment, meet.getHostId(), userId);

        comment.softDelete(userId);

        List<Long> commentIdsToDelete = new ArrayList<>();
        commentIdsToDelete.add(comment.getId());

        try {
            commentRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = commentRepository.findVersion(comment.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        if (type == CommentTarget.POST) {
            List<Long> replyIds = postCommentService.preparePostCommentDeletion(comment);

            if (replyIds != null) {
                commentIdsToDelete.addAll(replyIds);
            }
        }

        commentRepository.softDeleteAll(Status.DELETED, commentIdsToDelete, userId, LocalDateTime.now());
        generateCommentsDeletedEvent(commentIdsToDelete, type, targetId, userId);
    }

    private void generateCommentsDeletedEvent(List<Long> commentIds, CommentTarget type, Long targetId, Long deletedBy) {
        CommentsSoftDeletedEvent deletedEvent = CommentsSoftDeletedEvent.builder()
                .target(type)
                .targetId(targetId)
                .commentIds(commentIds)
                .commentsDeletedBy(deletedBy)
                .build();

        outboxService.save(COMMENTS_SOFT_DELETED, type.toAggregateType(), targetId, deletedEvent);
    }

    private Long getMeetId(CommentTarget type, Long targetId) {
        if (type == CommentTarget.POST) {
            boolean existsInPlan = planRepository.existsByIdAndStatus(targetId, Status.ACTIVE);

            if (existsInPlan) {
                return reader.findPlan(targetId).getMeetId();
            }
            return reader.findReviewByPostId(targetId).getMeetId();
        }

        if (type == CommentTarget.NOTICE) {
            return reader.findNotice(targetId).getMeetId();
        }

        throw new IllegalStatesException(ILLEGAL_ENUM_TYPE);
    }

    @Transactional
    public void commentReport(Long userId, CommentReportRequest request) {

//        같은 신고라도 사유가 다를 수 있으니 중복 신고를 허용
//        commentReportRepository.findByReporterIdAndCommentId(userId, sendRequest.commentId()).orElseThrow(
//                () -> new ResourceNotFoundException(CURRENT_REPORT)
//        );

        commentReportRepository.save(CommentReport.builder()
                .reason(request.reason())
                .commentId(request.commentId())
                .reporterId(userId)
                .build()
        );
    }
}
