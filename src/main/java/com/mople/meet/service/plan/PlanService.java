package com.mople.meet.service.plan;

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
import com.mople.global.enums.Status;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.PlanReport;
import com.mople.entity.user.User;
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

import static com.mople.dto.client.PlanClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofParticipants;
import static com.mople.dto.response.meet.plan.PlanViewResponse.ofPlanView;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class PlanService {

    private static final int PLAN_HOME_VIEW_SIZE = 5;
    private static final int PLAN_CURSOR_FIELD_COUNT = 1;
    private static final int PLAN_PARTICIPANT_CURSOR_FIELD_COUNT = 2;

    private final MeetPlanRepository meetPlanRepository;
    private final MeetMemberRepository memberRepository;
    private final PlanReportRepository planReportRepository;
    private final PlanParticipantRepository planParticipantRepository;
    private final ParticipantRepositorySupport participantRepositorySupport;
    private final PlanRepositorySupport planRepositorySupport;
    private final CommentRepositorySupport commentRepositorySupport;
    private final MeetRepositorySupport meetRepositorySupport;
    private final UserRepository userRepository;

    private final EntityReader reader;
    private final OutboxService outboxService;

    @Cacheable(cacheNames = "homeViewPlan", key = "#userId")
    @Transactional(readOnly = true)
    public PlanHomeViewResponse getPlanView(Long userId) {
        reader.findUser(userId);

        List<PlanViewResponse> homeViewPlan = planRepositorySupport.findHomeViewPlan(userId, PLAN_HOME_VIEW_SIZE);

        return new PlanHomeViewResponse(
                ofViews(homeViewPlan),
                meetRepositorySupport.findMeetUseMember(userId)
        );
    }

    @Transactional
    public PlanClientResponse createPlan(
            Long creatorId,
            PlanCreateRequest request
    ) {
        var user = reader.findUser(creatorId);
        var meet = reader.findMeet(request.meetId());

        if (!memberRepository.existsByMeetIdAndUserId(request.meetId(), creatorId)) {
            throw new AuthException(NOT_CREATOR);
        }

        MeetPlan plan = meetPlanRepository.save(
                MeetPlan.builder()
                        .name(request.name())
                        .planTime(request.planTime())
                        .address(request.planAddress())
                        .title(request.title())
                        .longitude(request.lot())
                        .latitude(request.lat())
                        .weatherAddress(request.weatherAddress())
                        .creatorId(user.getId())
                        .meetId(meet.getId())
                        .build()
        );

        if (request.planTime().isBefore(LocalDateTime.now().plusDays(5))) {
            WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                    .planId(plan.getId())
                    .build();

            outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, plan.getId(), requestedEvent);
        }

        planParticipantRepository.save(
                PlanParticipant.builder()
                        .userId(user.getId())
                        .planId(plan.getId())
                        .build()
        );

        PlanCreatedEvent createEvent = PlanCreatedEvent.builder()
                .meetId(plan.getMeetId())
                .planId(plan.getId())
                .planTime(plan.getPlanTime())
                .planCreatorId(plan.getCreatorId())
                .build();

        outboxService.save(PLAN_CREATED, PLAN, plan.getId(), createEvent);

        Integer participantCount = planParticipantRepository.countByPlanId(plan.getId());

        return ofView(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                commentRepositorySupport.countComment(plan.getId()));
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

        boolean changedPlace = plan.updatePlan(request);

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

        if (changedPlace || newTime.isBefore(LocalDateTime.now().plusDays(5))) {
            WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                    .planId(plan.getId())
                    .build();

            outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, plan.getId(), requestedEvent);
        }

        if (newTime.isAfter(LocalDateTime.now().plusDays(5))) {
            meetPlanRepository.deleteWeather(plan.getId());
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

        Integer participantCount = planParticipantRepository.countByPlanId(plan.getId());

        return ofView(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                commentRepositorySupport.countComment(plan.getId()));
    }

    @Transactional
    public void deletePlan(Long userId, Long planId) {
        reader.findUser(userId);
        var plan = reader.findPlan(planId);

        if (plan.isCreator(userId)) {
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

        Integer participantCount = planParticipantRepository.countByPlanId(plan.getId());

        return ofViewAndParticipant(
                ofPlanView(
                        plan,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount
                ),
                planParticipantRepository.existsByPlanIdAndUserId(planId, userId),
                commentRepositorySupport.countComment(plan.getId())
        );
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<PlanClientResponse> getPlanList(Long userId, Long meetId, CursorPageRequest request) {
        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<PlanListResponse> plans = getPlans(userId, meetId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                meetPlanRepository.countByMeetIdAndStatus(meetId, Status.ACTIVE),
                buildPlanCursorPage(size, plans)
        );
    }

    private List<PlanListResponse> getPlans(Long userId, Long meetId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, PLAN_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validatePlanCursor(cursorId);
        }

        return planRepositorySupport.findPlanPage(userId, meetId, cursorId, size);
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
        Meet meet = reader.findMeet(plan.getMeetId());

        if (!memberRepository.existsByMeetIdAndUserId(plan.getMeetId(), userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        Long hostId = meet.getCreatorId();
        Long creatorId = plan.getCreatorId();
        int size = request.getSafeSize();
        List<PlanParticipant> participants = getPlanParticipants(planId, hostId, creatorId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                planParticipantRepository.countByPlanId(plan.getId()),
                buildParticipantCursorPage(size, participants, hostId, creatorId)
        );
    }

    private List<PlanParticipant> getPlanParticipants(Long planId, Long hostId, Long creatorId, String encodedCursor, int size) {

        MemberCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, PLAN_PARTICIPANT_CURSOR_FIELD_COUNT);

            String cursorNickname = decodeParts[0];
            Long cursorId = Long.valueOf(decodeParts[1]);
            validateParticipantCursor(cursorNickname, cursorId);

            cursor = new MemberCursor(cursorNickname, cursorId, hostId, creatorId);
        }

        return participantRepositorySupport.findPlanParticipantPage(planId, hostId, creatorId, cursor, size);
    }

    private CursorPageResponse<UserRoleClientResponse> buildParticipantCursorPage(int size, List<PlanParticipant> participants, Long hostId, Long creatorId) {
        List<Long> userIds = participants.stream()
                .map(PlanParticipant::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                participants,
                size,
                p -> {
                    User user = reader.findUser(p.getUserId());
                    return new String[]{
                            user.getNickname(),
                            p.getId().toString()
                    };
                },
                list -> ofParticipants(list, userInfoById, hostId, creatorId)
        );
    }

    private void validateParticipantCursor(String cursorNickname, Long cursorId) {
        if (participantRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public void joinPlanParticipant(Long userId, Long planId) {
        reader.findUser(userId);
        reader.findPlan(planId);

        if (planParticipantRepository.existsByPlanIdAndUserId(planId, userId)) {
            throw new BadRequestException(CURRENT_PARTICIPANT);
        }

        var planParticipant = PlanParticipant.builder()
                .planId(planId)
                .userId(userId)
                .build();

        planParticipantRepository.save(planParticipant);
    }

    @Transactional
    public void deletePlanParticipant(Long userId, Long planId) {
        reader.findPlan(planId);
        reader.findUser(userId);

        if (!planParticipantRepository.existsByPlanIdAndUserId(planId, userId)) {
            throw new AuthException(NOT_PARTICIPANT);
        }

        planParticipantRepository.deleteByPlanIdAndUserId(planId, userId);
    }
}
