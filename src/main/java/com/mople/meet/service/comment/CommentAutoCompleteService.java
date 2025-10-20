package com.mople.meet.service.comment;

import com.mople.core.exception.custom.CursorException;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.custom.AutoCompleteCursor;
import com.mople.entity.meet.MeetMember;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mople.dto.client.UserRoleClientResponse.ofMembers;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class CommentAutoCompleteService {

    private static final int MEET_MEMBER_CURSOR_FIELD_COUNT = 1;

    private final MeetMemberRepositorySupport memberRepositorySupport;
    private final MeetMemberRepository memberRepository;
    private final UserRepository userRepository;

    public List<MeetMember> getMeetMembers(Long meetId, String keyword, String encodedCursor, int size) {

        AutoCompleteCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

            Long cursorId = Long.parseLong(decodeParts[0]);
            MeetMember member = memberRepository.findById(cursorId)
                    .orElseThrow(() -> new CursorException(INVALID_CURSOR));

            if (!Objects.equals(member.getMeetId(), meetId)) {
                throw new CursorException(INVALID_CURSOR);
            }

            cursor = new AutoCompleteCursor(
                    member.getRoleOrder(),
                    member.getNicknameLower(),
                    member.getId()
            );
        }

        return memberRepositorySupport.findMemberAutoCompletePage(meetId, keyword, cursor, size);
    }

    public CursorPageResponse<UserRoleClientResponse> buildAutoCompleteCursorPage(int size, List<MeetMember> members) {
        List<Long> userIds = members.stream()
                .map(MeetMember::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                members,
                size,
                m -> new String[]{
                        m.getId().toString()
                },
                list -> ofMembers(list, userInfoById)
        );
    }
}
