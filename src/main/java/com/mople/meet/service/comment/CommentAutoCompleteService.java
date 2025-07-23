package com.mople.meet.service.comment;

import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.AutoCompleteClientResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.entity.meet.MeetMember;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class CommentAutoCompleteService {

    private static final int MEET_MEMBER_CURSOR_FIELD_COUNT = 2;

    private final MeetMemberRepositorySupport memberRepositorySupport;
    private final EntityReader reader;

    public List<MeetMember> getMeetMembers(Long postId, String keyword, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            Long meetId = getMeetId(postId);
            return memberRepositorySupport.findMemberAutoCompleteFirstPage(meetId, keyword, size);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

        String cursorNickname = decodeParts[0];
        Long cursorId = Long.parseLong(decodeParts[1]);

        validateCursor(cursorNickname, cursorId);

        return memberRepositorySupport.findMemberAutoCompleteNextPage(postId, keyword, cursorNickname, cursorId, size);
    }

    private Long getMeetId(Long postId) {
        try {
            return reader.findPlan(postId).getMeet().getId();
        } catch (ResourceNotFoundException e) {
            return reader.findReviewByPostId(postId).getMeet().getId();
        }
    }

    public CursorPageResponse<AutoCompleteClientResponse> buildAutoCompleteCursorPage(int size, List<MeetMember> memberNextPage) {
        return buildCursorPage(
                memberNextPage,
                size,
                c -> new String[]{
                        c.getUser().getNickname(),
                        c.getId().toString()
                },
                AutoCompleteClientResponse::ofTargets
        );
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (memberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }
}
