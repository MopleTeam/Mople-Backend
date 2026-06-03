package com.mople.meet.service.notice;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.dto.request.meet.notice.NoticeUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.global.enums.Status;
import com.mople.global.enums.notice.NoticeType;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.notice.NoticeRepositorySupport;
import com.mople.meet.repository.notice.MeetNoticeRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.dto.client.NoticeClientResponse.ofNotice;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int NOTICE_CURSOR_FIELD_COUNT = 1;

    private final EntityReader reader;
    private final MeetNoticeRepository meetNoticeRepository;
    private final MeetMemberRepository memberRepository;
    private final NoticeRepositorySupport noticeRepositorySupport;

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
        return buildCursorPage(
                notices,
                size,
                n -> new String[]{
                        n.getId().toString()
                },
                NoticeClientResponse::ofNotices
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

        return ofNotice(notice);
    }

    private void validateCursor(Long cursorId) {
        if (noticeRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public NoticeClientResponse createNotice(Long userId, NoticeCreateRequest request) {
        Long meetId = request.meetId();

        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        MeetNotice customNotice = meetNoticeRepository.save(
                MeetNotice.ofCustom(request.content(), userId, meetId)
        );

        return ofNotice(customNotice);
    }

    @Transactional
    public NoticeClientResponse updateNotice(Long userId, Long noticeId, NoticeUpdateRequest request) {
        Long meetId = request.meetId();

        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        MeetNotice customNotice = meetNoticeRepository.findByIdAndStatus(noticeId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTICE));

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

        return ofNotice(customNotice);
    }

    @Transactional
    public void removeNotice(Long userId, Long noticeId) {
        reader.findUser(userId);

        MeetNotice customNotice = meetNoticeRepository.findByIdAndStatus(noticeId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTICE));

        Meet meet = reader.findMeet(customNotice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        meet.softDelete(userId);
        //todo. purge 이벤트 발행
    }

    @Transactional
    public NoticeClientResponse pinNotice(Long userId, Long noticeId) {
        reader.findUser(userId);

        MeetNotice notice = meetNoticeRepository.findByIdAndStatus(noticeId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTICE));

        Meet meet = reader.findMeet(notice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        if (notice.isPinned()) {
            return ofNotice(notice);
        }

        meetNoticeRepository.unpinAllByMeetId(meet.getId());

        notice.pin();
        meetNoticeRepository.flush();

        return ofNotice(notice);
    }

    @Transactional
    public NoticeClientResponse unpinNotice(Long userId, Long noticeId) {
        reader.findUser(userId);

        MeetNotice notice = meetNoticeRepository.findByIdAndStatus(noticeId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTICE));

        Meet meet = reader.findMeet(notice.getMeetId());

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        if (!notice.isPinned()) {
            return ofNotice(notice);
        }

        notice.unpin();
        meetNoticeRepository.flush();

        return ofNotice(notice);
    }
}
