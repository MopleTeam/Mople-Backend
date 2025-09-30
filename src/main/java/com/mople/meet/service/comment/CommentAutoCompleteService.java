package com.mople.meet.service.comment;

import com.mople.core.exception.custom.CursorException;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.AutoCompleteCursor;
import com.mople.entity.meet.MeetMember;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.mople.dto.client.UserRoleClientResponse.ofAutoCompleteUsers;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class CommentAutoCompleteService {

    private static final int MEET_MEMBER_CURSOR_FIELD_COUNT = 2;

    private final MeetMemberRepositorySupport memberRepositorySupport;
    private final UserRepository userRepository;
    private final EntityReader reader;

    public List<MeetMember> getMeetMembers(Long meetId, Long hostId, Long creatorId, String keyword, String encodedCursor, int size) {

        AutoCompleteCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

            String cursorNickname = decodeParts[0];
            Long cursorId = Long.parseLong(decodeParts[1]);
            validateCursor(cursorNickname, cursorId);

            cursor = new AutoCompleteCursor(cursorNickname, keyword, cursorId, hostId, creatorId);
        }

        return memberRepositorySupport.findMemberAutoCompletePage(meetId, hostId, creatorId, keyword, cursor, size);
    }

    public CursorPageResponse<UserRoleClientResponse> buildAutoCompleteCursorPage(int size, List<MeetMember> members, Long hostId, Long creatorId) {
        List<Long> userIds = members.stream()
                .map(MeetMember::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                members,
                size,
                m -> {
                    User user = reader.findUser(m.getUserId());

                    return new String[]{
                            user.getNickname(),
                            m.getId().toString()
                    };
                },
                list -> ofAutoCompleteUsers(list, userInfoById, hostId, creatorId)
        );
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (memberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
