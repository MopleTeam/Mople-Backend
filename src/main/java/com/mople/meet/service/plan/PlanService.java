package com.mople.meet.service.plan;

import com.mople.core.annotation.cache.InvalidateCache;
import com.mople.core.exception.custom.*;
import com.mople.dto.client.PlanClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.domain.global.WeatherRefreshRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanCreatedEvent;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.dto.event.data.domain.plan.PlanTimeChangedEvent;
import com.mople.dto.request.meet.plan.PlanReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.dto.response.meet.plan.*;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.CommentTarget;
import com.mople.global.enums.Status;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.utils.cursor.custom.UserCursor;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.PlanReport;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.impl.plan.ParticipantRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.impl.plan.PlanRepositorySupport;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.dto.request.meet.plan.PlanCreateRequest;
import com.mople.dto.request.meet.plan.PlanUpdateRequest;
import com.mople.meet.repository.plan.PlanReportRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

import org.hibernate.StaleObjectStateException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.mople.dto.client.PlanClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofParticipants;
import static com.mople.dto.response.meet.plan.PlanViewResponse.ofPlanView;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;
import static com.mople.global.utils.cursor.custom.UserCursor.forPlan;

@Service
@RequiredArgsConstructor
public class PlanService {

    private static final int PLAN_HOME_VIEW_SIZE = 5;
    private static final int PLAN_CURSOR_FIELD_COUNT = 1;
    private static final int PLAN_PARTICIPANT_CURSOR_FIELD_COUNT = 1;

    private final MeetPlanRepository meetPlanRepository;
    private final MeetMemberRepository memberRepository;
    private final PlanReportRepository planReportRepository;
    private final PlanParticipantRepository participantRepository;
    private final ParticipantRepositorySupport participantRepositorySupport;
    private final PlanRepositorySupport planRepositorySupport;
    private final CommentRepositorySupport commentRepositorySupport;
    private final MeetRepositorySupport meetRepositorySupport;
    private final UserRepository userRepository;

    private final EntityReader reader;
    private final OutboxService outboxService;

    // 강제 업데이트 시 삭제할 것
    @Cacheable(cacheNames = "homeViewPlan", key = "#userId")
    @Transactional(readOnly = true)
    public PlanHomeViewResponse_old getPlanView_old(Long userId) {
        reader.findUser(userId);

        List<PlanViewResponse> homeViewPlan = planRepositorySupport.findHomeViewPlan(userId, PLAN_HOME_VIEW_SIZE);

        return new PlanHomeViewResponse_old(
                ofViews(homeViewPlan),
                meetRepositorySupport.findMeetUseMember(userId)
        );
    }

    @Cacheable(cacheNames = "homeViewPlan", key = "#userId")
    @Transactional(readOnly = true)
    public PlanHomeViewResponse getPlanView(Long userId) {
        reader.findUser(userId);

        List<PlanViewResponse> homeViewPlan = planRepositorySupport.findHomeViewPlan(userId, PLAN_HOME_VIEW_SIZE);

        return new PlanHomeViewResponse(
                ofViews(homeViewPlan),
                meetRepositorySupport.hasJoinedMeet(userId)
        );
    }

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"#userId"}
    )
    @Transactional
    public PlanClientResponse createPlan(Long userId, PlanCreateRequest request) {
        var user = reader.findUser(userId);
        var meet = reader.findMeet(request.meetId());

        if (!memberRepository.existsByMeetIdAndUserId(request.meetId(), userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        MeetPlan plan = meetPlanRepository.save(
                MeetPlan.builder()
                        .name(request.name())
                        .planTime(request.planTime())
                        .address(request.planAddress())
                        .title(request.title())
                        .description(request.description())
                        .longitude(request.lot())
                        .latitude(request.lat())
                        .weatherAddress(request.weatherAddress())
                        .creatorId(user.getId())
                        .meetId(meet.getId())
                        .build()
        );

        if (request.lat() != null && request.lot() != null && request.planTime().isBefore(LocalDateTime.now().plusDays(5))) {
            WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                    .planId(plan.getId())
                    .build();

            outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, plan.getId(), requestedEvent);
        }

        participantRepository.save(
                PlanParticipant.builder()
                        .userId(user.getId())
                        .planId(plan.getId())
                        .nickName(user.getNickname())
                        .hostId(meet.getHostId())
                        .creatorId(plan.getCreatorId())
                        .build()
        );

        PlanCreatedEvent createEvent = PlanCreatedEvent.builder()
                .meetId(plan.getMeetId())
                .planId(plan.getId())
                .planTime(plan.getPlanTime())
                .planCreatorId(plan.getCreatorId())
                .build();

        outboxService.save(PLAN_CREATED, PLAN, plan.getId(), createEvent);

        Integer participantCount = participantRepository.countByPlanId(plan.getId());

        return ofView(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                true,
                commentRepositorySupport.countTotalComment(CommentTarget.POST, plan.getId()));
    }

    @Transactional
    public PlanClientResponse updatePlan(Long userId, PlanUpdateRequest request) {
        MeetPlan plan = reader.findPlan(request.planId());
        Meet meet = reader.findMeet(plan.getMeetId());

        if (plan.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        LocalDateTime newTime = request.planTime();
        LocalDateTime oldTime = plan.getPlanTime();

        boolean changedLocation = plan.updatePlan(request) && plan.hasLocation();

        try {
            meetPlanRepository.flush();

        } catch (
                OptimisticLockException
                 | OptimisticLockingFailureException
                 | StaleObjectStateException e
        ) {
            long currentVersion = meetPlanRepository.findVersion(plan.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        boolean oldWithin = oldTime.isBefore(LocalDateTime.now().plusDays(5));
        boolean newWithin = newTime.isBefore(LocalDateTime.now().plusDays(5));
        boolean crossedIntoWindow = (!oldWithin && newWithin) && plan.hasLocation();

        if (changedLocation || crossedIntoWindow) {
            WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                    .planId(plan.getId())
                    .build();

            outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, plan.getId(), requestedEvent);
        }

        if (!plan.hasLocation() || newTime.isAfter(LocalDateTime.now().plusDays(5))) {
            meetPlanRepository.deleteWeather(plan.getId());
            plan = reader.findPlan(plan.getId());
        }

        if (!newTime.equals(oldTime)) {
            PlanTimeChangedEvent changedEvent = PlanTimeChangedEvent.builder()
                    .planId(plan.getId())
                    .timeChangedBy(userId)
                    .newTime(newTime)
                    .oldTime(oldTime)
                    .build();

            outboxService.save(PLAN_TIME_CHANGED, PLAN, plan.getId(), changedEvent);
        }

        Integer participantCount = participantRepository.countByPlanId(plan.getId());

        return ofView(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                true,
                commentRepositorySupport.countTotalComment(CommentTarget.POST, plan.getId()));
    }

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"@planParticipantRepository.findUserIdsByPlanId(#planId)"}
    )
    @Transactional
    public void deletePlan(Long userId, Long planId) {
        reader.findUser(userId);
        var plan = reader.findPlan(planId);
        Meet meet = reader.findMeet(plan.getMeetId());

        if (plan.isCreator(userId) && !meet.getHostId().equals(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        plan.softDelete(userId);

        try {
            meetPlanRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = meetPlanRepository.findVersion(plan.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        PlanSoftDeletedEvent deleteEvent = PlanSoftDeletedEvent.builder()
                .planId(plan.getId())
                .planDeletedBy(userId)
                .cause(DeletionCause.DIRECT_PLAN_DELETE)
                .build();

        outboxService.save(PLAN_SOFT_DELETED, PLAN, plan.getId(), deleteEvent);
    }

    @Transactional(readOnly = true)
    public PlanClientResponse getPlanDetail(Long userId, Long planId) {
        var plan = reader.findPlan(planId);
        Meet meet = reader.findMeet(plan.getMeetId());
        reader.findUser(userId);

        if (!memberRepository.existsByMeetIdAndUserId(plan.getMeetId(), userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        Integer participantCount = participantRepository.countByPlanId(plan.getId());

        return ofView(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                participantRepository.existsByPlanIdAndUserId(planId, userId),
                commentRepositorySupport.countTotalComment(CommentTarget.POST, plan.getId())
        );
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<PlanClientResponse> getPlanList(Long userId, Long meetId, CursorPageRequest request) {
        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<MeetPlan> plans = getPlans(meetId, request.cursor(), size);

        List<Long> planIds = plans.stream()
                .map(MeetPlan::getId)
                .toList();

        Map<Long, Integer> participantCountMap = participantRepositorySupport.planParticipantCountMap(planIds);
        Set<Long> joinedPlanIds = participantRepositorySupport.findJoinedPlanIds(userId, planIds);

        List<PlanListResponse> responses = plans.stream()
                .map((p) ->
                        new PlanListResponse(
                                meet,
                                p,
                                participantCountMap.getOrDefault(p.getId(), 0),
                                joinedPlanIds.contains(p.getId())
                        )
                )
                .toList();

        return FlatCursorPageResponse.of(
                meetPlanRepository.countByMeetIdAndStatus(meetId, Status.ACTIVE),
                buildPlanCursorPage(size, responses)
        );
    }

    private List<MeetPlan> getPlans(Long meetId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, PLAN_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validatePlanCursor(cursorId);
        }

        return planRepositorySupport.findPlanPage(meetId, cursorId, size);
    }

    private void validatePlanCursor(Long cursorId) {
        if (planRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    private CursorPageResponse<PlanClientResponse> buildPlanCursorPage(int size, List<PlanListResponse> planListResponses) {
        return buildCursorPage(
                planListResponses,
                size,
                r -> new String[]{
                        r.planId().toString()
                },
                PlanClientResponse::ofLists
        );
    }

    @Transactional(readOnly = true)
    public UserAllDateResponse getAllDates(Long userId) {
        return planRepositorySupport.getAllDate(userId);
    }

    @Transactional(readOnly = true)
    public UserPageResponse getPlanPages(Long userId, YearMonth date) {
        return planRepositorySupport.getPlanAndReviewPages(userId, date);
    }

    @Transactional
    public void reportPlan(Long userId, PlanReportRequest request) {
        planReportRepository.save(
                PlanReport.builder()
                        .reason(request.reason())
                        .planId(request.planId())
                        .reporterId(userId)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<UserRoleClientResponse> getParticipantList(Long userId, Long planId, CursorPageRequest request) {
        MeetPlan plan = reader.findPlan(planId);

        if (!memberRepository.existsByMeetIdAndUserId(plan.getMeetId(), userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<PlanParticipant> participants = getPlanParticipants(planId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                participantRepository.countByPlanId(plan.getId()),
                buildParticipantCursorPage(size, participants)
        );
    }

    private List<PlanParticipant> getPlanParticipants(Long planId, String encodedCursor, int size) {

        UserCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, PLAN_PARTICIPANT_CURSOR_FIELD_COUNT);

            Long cursorId = Long.valueOf(decodeParts[0]);
            PlanParticipant participant = participantRepository.findById(cursorId)
                    .orElseThrow(() -> new CursorException(INVALID_CURSOR));

            if (!Objects.equals(participant.getPlanId(), planId)) {
                throw new CursorException(INVALID_CURSOR);
            }

            cursor = forPlan(participant);
        }

        return participantRepositorySupport.findPlanParticipantPage(planId, cursor, size);
    }

    private CursorPageResponse<UserRoleClientResponse> buildParticipantCursorPage(int size, List<PlanParticipant> participants) {
        List<Long> userIds = participants.stream()
                .map(PlanParticipant::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                participants,
                size,
                p ->
                    new String[]{
                            p.getId().toString()
                    },
                list -> ofParticipants(list, userInfoById)
        );
    }

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"#userId"}
    )
    @Transactional
    public void joinPlanParticipant(Long userId, Long planId) {
        User user = reader.findUser(userId);
        MeetPlan plan = reader.findPlan(planId);
        Meet meet = reader.findMeet(plan.getMeetId());

        if (participantRepository.existsByPlanIdAndUserId(planId, userId)) {
            throw new BadRequestException(CURRENT_PARTICIPANT);
        }

        var planParticipant = PlanParticipant.builder()
                .planId(planId)
                .userId(userId)
                .nickName(user.getNickname())
                .hostId(meet.getHostId())
                .creatorId(plan.getCreatorId())
                .build();

        participantRepository.save(planParticipant);
    }

    @InvalidateCache(
            cacheName = "homeViewPlan",
            keys = {"#userId"}
    )
    @Transactional
    public void deletePlanParticipant(Long userId, Long planId) {
        reader.findPlan(planId);
        reader.findUser(userId);

        if (!participantRepository.existsByPlanIdAndUserId(planId, userId)) {
            throw new AuthException(NOT_PARTICIPANT);
        }

        participantRepository.deleteByPlanIdAndUserId(planId, userId);
    }
}
