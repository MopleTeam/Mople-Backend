package com.mople.meet.service.comment;

import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.AutoCompleteClientResponse;
import com.mople.dto.response.pagination.CursorPage;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.entity.meet.MeetMember;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mople.dto.client.AutoCompleteClientResponse.ofTargets;
import static com.mople.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class CommentAutoCompleteService {

    private final MeetMemberRepositorySupport memberRepositorySupport;
    private final EntityReader reader;

    public CursorPageResponse<AutoCompleteClientResponse> getMeetMembers(Long postId, String keyword, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            Long meetId = getMeetId(postId);
            List<MeetMember> memberFirstPage = memberRepositorySupport.findMemberFirstPage(meetId, keyword, size);

            return buildCursorPage(size, memberFirstPage);
        }

        String[] parts = CursorUtils.decode(encodedCursor).split("\\|");
        String cursorNickname = parts[0];
        Long cursorId = Long.parseLong(parts[1]);

        validateCursor(cursorNickname, cursorId);

        List<MeetMember> memberNextPage = memberRepositorySupport.findMemberNextPage(postId, keyword, cursorNickname, cursorId, size);
        return buildCursorPage(size, memberNextPage);
    }

    private CursorPageResponse<AutoCompleteClientResponse> buildCursorPage(int size, List<MeetMember> members) {
        boolean hasNext = members.size() > size;
        members = hasNext ? members.subList(0, size) : members;

        String nextCursor = null;
        if (hasNext && !members.isEmpty()) {
            MeetMember last = members.get(members.size() - 1);
            nextCursor = CursorUtils.encode(last.getUser().getNickname() + "|" + last.getUser().getId());
        }

        CursorPage page = CursorPage.builder()
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(members.size())
                .build();

        return CursorPageResponse.of(ofTargets(members), page);
    }

    private Long getMeetId(Long postId) {
        try {
            return reader.findPlan(postId).getMeet().getId();
        } catch (ResourceNotFoundException e) {
            return reader.findReviewByPostId(postId).getMeet().getId();
        }
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (memberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }
    }
}
