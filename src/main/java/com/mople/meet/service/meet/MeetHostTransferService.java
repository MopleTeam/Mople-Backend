package com.mople.meet.service.meet;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.CursorException;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.event.data.domain.meet.HostChangedEvent;
import com.mople.dto.request.meet.HostChangeRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.MeetListResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.MeetMember;
import com.mople.global.enums.UserRole;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.MEET_HOST_CHANGED;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class MeetHostTransferService {

    private static final int MEET_CURSOR_FIELD_COUNT = 1;

    private final MeetRepositorySupport meetRepositorySupport;
    private final MeetMemberRepository meetMemberRepository;
    private final OutboxService outboxService;
    private final EntityReader reader;

    public Meet changeMeetHost(Long userId, Long meetId, HostChangeRequest request) {
        Long newHostId = request.newHostId();

        reader.findUser(userId);
        reader.findUser(newHostId);
        Meet meet = reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId) ||
                !meetMemberRepository.existsByMeetIdAndUserId(meetId, newHostId)
        ) {
            throw new BadRequestException(NOT_MEMBER);
        }

        if (!meet.matchHost(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        if (newHostId.equals(userId)) {
            throw new BadRequestException(CURRENT_HOST);
        }

        meet.changeHost(newHostId);

        MeetMember oldHostMember = meetMemberRepository.findMeetIdAndUserId(meetId, userId);
        MeetMember newHostMember = meetMemberRepository.findMeetIdAndUserId(meetId, newHostId);

        oldHostMember.changeRole(UserRole.PARTICIPANT);
        newHostMember.changeRole(UserRole.HOST);

        HostChangedEvent changedEvent = HostChangedEvent.builder()
                .meetId(meetId)
                .oldHostId(userId)
                .newHostId(newHostId)
                .build();

        outboxService.save(MEET_HOST_CHANGED, MEET, meetId, changedEvent);

        return meet;
    }

    public CursorPageResponse<MeetClientResponse> getHostedMeet(Long userId, CursorPageRequest request) {
        reader.findUser(userId);

        int size = request.getSafeSize();
        List<Meet> meets = getHostedMeets(userId, request.cursor(), size);

        List<MeetListResponse> meetListResponses = meetRepositorySupport.mapToMeetListResponses(meets);

        return buildMeetCursorPage(size, meetListResponses);
    }

    private List<Meet> getHostedMeets(Long userId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validateCursor(cursorId);
        }

        return meetRepositorySupport.findHostedMeetPage(userId, cursorId, size);
    }

    private CursorPageResponse<MeetClientResponse> buildMeetCursorPage(int size, List<MeetListResponse> meetListResponses) {
        return buildCursorPage(
                meetListResponses,
                size,
                r -> new String[]{
                        r.meetId().toString()
                },
                MeetClientResponse::ofListMeets
        );
    }

    private void validateCursor(Long cursorId) {
        if (meetRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
