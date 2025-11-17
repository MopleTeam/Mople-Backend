package com.mople.meet.service.notice;

import com.mople.core.exception.custom.AuthException;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
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
    public NoticeClientResponse createNotice(Long userId, Long meetId, NoticeCreateRequest request) {
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
}
