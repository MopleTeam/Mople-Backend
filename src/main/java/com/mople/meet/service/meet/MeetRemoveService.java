package com.mople.meet.service.meet;

import com.mople.core.annotation.cache.InvalidateCache;
import com.mople.core.exception.custom.ConcurrencyConflictException;
import com.mople.entity.meet.Meet;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.MeetRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.global.enums.ExceptionReturnCode.REQUEST_CONFLICT;

@Service
@RequiredArgsConstructor
public class MeetRemoveService {

    private final MeetRepository meetRepository;
    private final MeetMemberRepository memberRepository;

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"@meetMemberRepository.findUserIdsByMeetId(#meet.id)"}
    )
    @Transactional
    public void removeMeetAsCreator(Meet meet, Long userId) {
        meet.softDelete(userId);

        try {
            meetRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = meetRepository.findVersion(meet.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }
    }

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"#userId"}
    )
    @Transactional
    public void removeMeetAsMember(Long meetId, Long userId) {
        memberRepository.deleteByMeetIdAndUserId(meetId, userId);
    }
}
