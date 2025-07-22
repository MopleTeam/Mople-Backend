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

    private final MeetMemberRepositorySupport memberRepositorySupport;
    private final EntityReader reader;

    public CursorPageResponse<AutoCompleteClientResponse> getMeetMembers(Long postId, String keyword, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            Long meetId = getMeetId(postId);
            List<MeetMember> memberFirstPage = memberRepositorySupport.findMemberAutoCompleteFirstPage(meetId, keyword, size);

            return buildAutoCompleteCursorPage(size, memberFirstPage);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor);
        validateCursor(decodeParts);

        String cursorNickname = decodeParts[0];
        Long cursorId = Long.parseLong(decodeParts[1]);

        List<MeetMember> memberNextPage = memberRepositorySupport.findMemberAutoCompleteNextPage(postId, keyword, cursorNickname, cursorId, size);

        return buildAutoCompleteCursorPage(size, memberNextPage);
    }

    private Long getMeetId(Long postId) {
        try {
            return reader.findPlan(postId).getMeet().getId();
        } catch (ResourceNotFoundException e) {
            return reader.findReviewByPostId(postId).getMeet().getId();
        }
    }

    private CursorPageResponse<AutoCompleteClientResponse> buildAutoCompleteCursorPage(int size, List<MeetMember> memberNextPage) {
        return buildCursorPage(
                memberNextPage,
                size,
                c -> new String[]{
                        c.getUser().getNickname(),
                        c.getUser().getId().toString()
                },
                AutoCompleteClientResponse::ofTargets
        );
    }

    private void validateCursor(String[] decodeParts) {
        if (decodeParts.length != 2) {
            throw new CursorException(INVALID_CURSOR);
        }

        try {
            Long.parseLong(decodeParts[1]);
        } catch (NumberFormatException e) {
            throw new CursorException(INVALID_CURSOR);
        }

        String cursorNickname = decodeParts[0];
        Long cursorId = Long.valueOf(decodeParts[1]);

        if (memberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }
    }
}
