package com.mople.meet.service.comment;

import com.mople.dto.client.comment.NoticeCommentClientResponse;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.comment.NoticeCommentResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.entity.user.User;
import com.mople.global.enums.CommentTarget;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.MeetCommentRepository;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mople.dto.client.comment.NoticeCommentClientResponse.ofNoticeComment;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class NoticeCommentService {

    private static final int COMMENT_CURSOR_FIELD_COUNT = 1;

    private final UserRepository userRepository;
    private final MeetCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;

    private final EntityReader reader;
    private final CommentValidator commentValidator;

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<NoticeCommentClientResponse> getNoticeCommentList(Long userId, Long noticeId, CursorPageRequest request) {
        MeetNotice notice = reader.findNotice(noticeId);
        Long meetId = notice.getMeetId();

        commentValidator.validateMember(userId, meetId);

        int size = request.getSafeSize();
        List<NoticeCommentResponse> noticeCommentResponses = getComments(noticeId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                commentRepositorySupport.countParentComments(CommentTarget.NOTICE, noticeId),
                buildCommentCursorPage(size, noticeCommentResponses)
        );
    }

    private List<NoticeCommentResponse> getComments(Long noticeId, String encodedCursor, int size) {
        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, COMMENT_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            commentValidator.validateCursor(cursorId);
        }

        List<MeetComment> commentPage = commentRepositorySupport.findCommentPage(CommentTarget.NOTICE, noticeId, cursorId, size);

        return mapToResponses(commentPage);
    }

    private CursorPageResponse<NoticeCommentClientResponse> buildCommentCursorPage(int size, List<NoticeCommentResponse> postCommentResponses) {
        return buildCursorPage(
                postCommentResponses,
                size,
                c -> new String[]{
                        c.commentId().toString()
                },
                NoticeCommentClientResponse::ofNoticeComments
        );
    }

    private List<NoticeCommentResponse> mapToResponses(List<MeetComment> comments) {
        List<Long> writerIds = comments.stream()
                .map(MeetComment::getWriterId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(writerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return comments.stream()
                .map(comment -> new NoticeCommentResponse(
                        comment,
                        userMap.get(comment.getWriterId())
                ))
                .toList();
    }

    @Transactional
    public NoticeCommentClientResponse createNoticeComment(Long userId, Long noticeId, CommentCreateRequest request) {
        reader.findUser(userId);
        MeetNotice notice = reader.findNotice(noticeId);
        Long meetId = notice.getMeetId();

        commentValidator.validateMember(userId, meetId);

        MeetComment comment = MeetComment.ofNotice(
                request.contents(),
                noticeId,
                LocalDateTime.now(),
                userId
        );
        commentRepository.save(comment);

        User writer = reader.findUser(comment.getWriterId());

        return ofNoticeComment(new NoticeCommentResponse(comment, writer));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public NoticeCommentClientResponse toNoticeCommentResponse(
            User writer,
            MeetComment comment
    ) {
        return ofNoticeComment(new NoticeCommentResponse(comment, writer));
    }
}
