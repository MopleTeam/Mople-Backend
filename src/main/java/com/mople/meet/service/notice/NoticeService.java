package com.mople.meet.service.notice;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.event.data.domain.notice.NoticeSoftDeletedEvent;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.dto.request.meet.notice.NoticeUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.enums.notice.NoticeType;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.notice.NoticeRepositorySupport;
import com.mople.meet.repository.notice.MeetNoticeRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mople.dto.client.NoticeClientResponse.ofNotice;
import static com.mople.dto.client.NoticeClientResponse.ofNotices;
import static com.mople.dto.response.user.UserInfo.of;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.event.AggregateType.NOTICE;
import static com.mople.global.enums.event.EventTypeNames.NOTICE_SOFT_DELETED;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int NOTICE_CURSOR_FIELD_COUNT = 1;

    private final EntityReader reader;
    private final MeetNoticeRepository meetNoticeRepository;
    private final MeetMemberRepository memberRepository;
    private final NoticeRepositorySupport noticeRepositorySupport;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional(readOnly = true)
    public CursorPageResponse<NoticeClientResponse> getNoticeList(Long userId, Long meetId, NoticeType type, CursorPageRequest request) {
        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<MeetNotice> notices = getNotices(meetId, type, request.cursor(), size);

        return buildNoticeCursorPage(size, notices);
    }

    private List<MeetNotice> getNotices(Long meetId, NoticeType type, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, NOTICE_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validateCursor(cursorId);
        }

        return noticeRepositorySupport.findNoticePage(meetId, type, cursorId, size);
    }

    private CursorPageResponse<NoticeClientResponse> buildNoticeCursorPage(int size, List<MeetNotice> notices) {
        List<Long> userIds = notices.stream()
                .map(MeetNotice::getCreatorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                notices,
                size,
                n -> new String[]{
                        n.getId().toString()
                },
                list -> ofNotices(list, userInfoById)
        );
    }

    @Transactional(readOnly = true)
    public NoticeClientResponse getSpecNotice(Long userId, Long noticeId) {
        reader.findUser(userId);
        MeetNotice notice = reader.findNotice(noticeId);
        Long meetId = notice.getMeetId();

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        if (notice.getType() == NoticeType.SYSTEM) {
            return ofNotice(notice, null);
        }

        User writer = reader.findUser(notice.getCreatorId());

        return ofNotice(notice, of(writer));
    }

    private void validateCursor(Long cursorId) {
        if (noticeRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public NoticeClientResponse createNotice(Long userId, NoticeCreateRequest request) {
        Long meetId = request.meetId();

        User user = reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        MeetNotice customNotice = meetNoticeRepository.save(
                MeetNotice.ofCustom(request.content(), userId, meetId)
        );

        return ofNotice(customNotice, of(user));
    }

    @Transactional
    public NoticeClientResponse updateNotice(Long userId, Long noticeId, NoticeUpdateRequest request) {
        Long meetId = request.meetId();

        User user = reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        MeetNotice customNotice = reader.findNotice(noticeId);
        if (!customNotice.getMeetId().equals(meetId)) {
            throw new BadRequestException(NOT_FOUND_NOTICE);
        }

        customNotice.updateNotice(request.content());

        try {
            meetNoticeRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = meetNoticeRepository.findVersion(meet.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        return ofNotice(customNotice, of(user));
    }

    @Transactional
    public void removeNotice(Long userId, Long noticeId) {
        reader.findUser(userId);
        MeetNotice customNotice = reader.findNotice(noticeId);
        Meet meet = reader.findMeet(customNotice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        customNotice.softDelete(userId);

        NoticeSoftDeletedEvent deleteEvent = NoticeSoftDeletedEvent.builder()
                .noticeId(customNotice.getId())
                .noticeDeletedBy(userId)
                .build();

        outboxService.save(NOTICE_SOFT_DELETED, NOTICE, customNotice.getId(), deleteEvent);
    }

    @Transactional
    public NoticeClientResponse pinNotice(Long userId, Long noticeId) {
        User user = reader.findUser(userId);
        MeetNotice notice = reader.findNotice(noticeId);
        Meet meet = reader.findMeet(notice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        if (notice.isPinned()) {
            return ofNotice(notice, of(user));
        }

        meetNoticeRepository.unpinAllByMeetId(meet.getId());

        notice.pin();
        meetNoticeRepository.flush();

        return ofNotice(notice, of(user));
    }

    @Transactional
    public NoticeClientResponse unpinNotice(Long userId, Long noticeId) {
        User user = reader.findUser(userId);
        MeetNotice notice = reader.findNotice(noticeId);
        Meet meet = reader.findMeet(notice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        if (!notice.isPinned()) {
            return ofNotice(notice, of(user));
        }

        notice.unpin();
        meetNoticeRepository.flush();

        return ofNotice(notice, of(user));
    }
}
