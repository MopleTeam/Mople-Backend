package com.mople.meet.service.notice;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.ConcurrencyConflictException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.dto.request.meet.notice.NoticeUpdateRequest;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.notice.NoticeRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.dto.client.NoticeClientResponse.ofNotice;
import static com.mople.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final EntityReader reader;
    private final NoticeRepository noticeRepository;

    @Transactional
    public NoticeClientResponse createNotice(Long userId, NoticeCreateRequest request) {
        Long meetId = request.meetId();

        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_HOST);
        }

        MeetNotice customNotice = noticeRepository.save(
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

        MeetNotice customNotice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTICE));

        customNotice.updateNotice(request.content());

        try {
            noticeRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = noticeRepository.findVersion(meet.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        return ofNotice(customNotice);
    }
}
