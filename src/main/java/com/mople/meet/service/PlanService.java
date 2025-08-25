package com.mople.meet.service;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.PlanClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.dto.request.meet.plan.PlanReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.dto.response.meet.plan.*;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.dto.response.weather.WeatherInfoResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.MeetTime;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.PlanReport;
import com.mople.entity.user.User;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.mapper.PlanMapper;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetTimeRepository;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.impl.plan.ParticipantRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.impl.plan.PlanRepositorySupport;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.dto.request.meet.plan.PlanCreateRequest;
import com.mople.dto.request.meet.plan.PlanUpdateRequest;
import com.mople.meet.repository.plan.PlanReportRepository;
import com.mople.meet.schedule.PlanScheduleJob;
import com.mople.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.mople.dto.client.PlanClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofParticipants;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private static final int PLAN_HOME_VIEW_SIZE = 5;
    private static final int PLAN_CURSOR_FIELD_COUNT = 1;
    private static final int PLAN_PARTICIPANT_CURSOR_FIELD_COUNT = 2;

    private final WeatherService weatherService;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReportRepository planReportRepository;
    private final PlanParticipantRepository planParticipantRepository;
    private final ParticipantRepositorySupport participantRepositorySupport;
    private final PlanRepositorySupport planRepositorySupport;
    private final MeetTimeRepository timeRepository;
    private final CommentRepositorySupport commentRepositorySupport;

    private final PlanMapper mapper;
    private final EntityReader reader;

    private final TaskScheduler taskScheduler;
    private final PlanScheduleJob planScheduleJob;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public PlanHomeViewResponse getPlanView(Long userId) {
        return new PlanHomeViewResponse(ofViews(planRepositorySupport.findHomeViewPlan(userId, PLAN_HOME_VIEW_SIZE)), reader.findMeetListUseMember(userId));
    }

    @Transactional
    public PlanClientResponse createPlan(
            Long creatorId,
            PlanCreateRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();

        var user = reader.findUser(creatorId);
        var meet = reader.findMeet(request.meetId());

        if (meet.matchMember(user.getId())) {
            throw new AuthException(NOT_FOUND_MEMBER);
        }

        MeetPlan plan =
                meetPlanRepository.save(
                        MeetPlan.builder()
                                .name(request.name())
                                .planTime(request.planTime())
                                .address(request.planAddress())
                                .title(request.title())
                                .longitude(request.lot())
                                .latitude(request.lat())
                                .weatherAddress(request.weatherAddress())
                                .creator(user)
                                .meet(meet)
                                .build()
                );

        PlanParticipant participant = PlanParticipant.builder()
                .user(user)
                .build();

        plan.addParticipant(participant);
        planParticipantRepository.save(participant);

        meet.addPlan(plan);

        timeRepository.save(new MeetTime(plan.getMeet().getId(), plan.getId(), request.planTime()));

        if (request.planTime().isBefore(now.plusDays(5))) {
            plan.updateWeather(getPlanWeather(request.lot(), request.lat(), request.planTime()));
        }

        publisher.publishEvent(
                NotifyEventPublisher.planNew(
                        PlanCreateNotifyEvent.builder()
                                .meetId(meet.getId())
                                .meetName(meet.getName())
                                .planId(plan.getId())
                                .planName(plan.getName())
                                .planTime(plan.getPlanTime())
                                .planCreatorId(user.getId())
                                .build()
                )
        );

        if (request.planTime().isAfter(now.plusHours(1))) {
            long hour = now.until(request.planTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;

            taskScheduler.schedule(
                    () -> planScheduleJob.planRemindSchedule(plan.getId(), plan.getPlanTime(), creatorId),
                    plan.getPlanTime().minusHours(hour).atZone(ZoneId.systemDefault()).toInstant()
            );
        }

        return ofView(mapper.getPlanView(plan), commentRepositorySupport.countComment(plan.getId()));
    }

    @Transactional
    public PlanClientResponse updatePlan(
            Long userId,
            PlanUpdateRequest request
    ) {
        var plan = reader.findPlan(request.planId());

        if (plan.isCreator(userId)) {
            throw new BadRequestException(NOT_CREATOR);
        }

        if (!plan.equalTime(request.planTime()) && request.planTime().isAfter(plan.getPlanTime().plusHours(1))) {
            long hour = request.planTime().until(plan.getPlanTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;

            taskScheduler.schedule(
                    () -> planScheduleJob.planRemindSchedule(plan.getId(), request.planTime(), userId),
                    request.planTime().minusHours(hour).atZone(ZoneId.systemDefault()).toInstant()
            );
        }

        MeetTime meetTime = timeRepository.findByPlanId(plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_TIME));

        meetTime.updateTime(request.planTime());

        if (plan.updatePlan(request) || request.planTime().isBefore(LocalDateTime.now().plusDays(5))) {
            plan.updateWeather(getPlanWeather(request.lot(), request.lat(), request.planTime()));
        }

        if (request.planTime().isAfter(LocalDateTime.now().plusDays(5))) {
            plan.deleteWeatherInfo();
        }

        publisher.publishEvent(
                NotifyEventPublisher.planUpdate(
                        PlanUpdateNotifyEvent.builder()
                                .meetId(plan.getMeet().getId())
                                .meetName(plan.getMeet().getName())
                                .planId(plan.getId())
                                .planName(plan.getName())
                                .planUpdatedBy(plan.getCreator().getId())
                                .build()
                )
        );
        return ofUpdate(mapper.getPlanView(plan), commentRepositorySupport.countComment(plan.getId()));
    }

    @Transactional
    public void deletePlan(Long userId, Long planId) {
        var plan = reader.findPlan(planId);

        if (plan.isCreator(userId)) {
            throw new BadRequestException(NOT_CREATOR);
        }

        timeRepository.deleteAllInBatch(timeRepository.findByAllPlanId(planId));

        publisher.publishEvent(
                NotifyEventPublisher.planRemove(
                        PlanDeleteNotifyEvent.builder()
                                .meetId(plan.getMeet().getId())
                                .meetName(plan.getMeet().getName())
                                .planId(plan.getId())
                                .planName(plan.getName())
                                .planDeletedBy(plan.getCreator().getId())
                                .build()
                )
        );
    }

    @Transactional(readOnly = true)
    public PlanClientResponse getPlanDetail(Long userId, Long planId) {
        var plan = reader.findPlan(planId);

        if (plan.getMeet().matchMember(userId)) {
            throw new AuthException(NOT_FOUND_MEMBER);
        }

        return ofViewAndParticipant(
                mapper.getPlanView(plan),
                planParticipantRepository.existsByPlanIdAndUserId(planId, userId),
                commentRepositorySupport.countComment(plan.getId())
        );
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<PlanClientResponse> getPlanList(Long userId, Long meetId, CursorPageRequest request) {
        validateMemberByMeetId(userId, meetId);

        int size = request.getSafeSize();
        List<PlanListResponse> plans = getPlans(userId, meetId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                planRepositorySupport.countPlans(meetId),
                buildPlanCursorPage(size, plans)
        );
    }

    private void validateMemberByMeetId(Long userId, Long meetId) {
        User user = reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (meet.matchMember(user.getId())) {
            throw new BadRequestException(NOT_MEMBER);
        }
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
                c -> new String[]{
                        c.planId().toString()
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
        validateMemberByPlanId(userId, planId);

        Long hostId = plan.getMeet().getCreator().getId();
        Long creatorId = plan.getCreator().getId();
        int size = request.getSafeSize();
        List<PlanParticipant> participants = getPlanParticipants(planId, hostId, creatorId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                participantRepositorySupport.countPlanParticipants(planId),
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
        return buildCursorPage(
                participants,
                size,
                c -> new String[]{
                        c.getUser().getNickname(),
                        c.getId().toString()
                },
                list -> ofParticipants(list, hostId, creatorId)
        );
    }

    private void validateMemberByPlanId(Long userId, Long planId) {
        User user = reader.findUser(userId);
        MeetPlan plan = reader.findPlan(planId);

        if (plan.getMeet().matchMember(user.getId())) {
            throw new BadRequestException(NOT_MEMBER);
        }
    }

    private void validateParticipantCursor(String cursorNickname, Long cursorId) {
        if (participantRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public void joinPlanParticipant(Long userId, Long planId) {
        var plan = reader.findPlan(planId);

        if (!plan.findParticipantInUser(userId)) {
            throw new BadRequestException(CURRENT_PARTICIPANT);
        }

        var user = reader.findUser(userId);

        var planParticipant = PlanParticipant.builder()
                .user(user)
                .build();

        plan.addParticipant(planParticipant);

        planParticipantRepository.save(planParticipant);
    }

    @Transactional
    public void deletePlanParticipant(Long userId, Long planId) {
        var plan = reader.findPlan(planId);

        var participant = plan.getParticipantById(userId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_PARTICIPANT)
        );

        plan.removeParticipant(userId);
    }

    public WeatherInfoResponse getPlanWeather(BigDecimal lot, BigDecimal lat, LocalDateTime planTime) {
        return weatherService
                .getClosestWeatherInfoFromDateTime(
                        new CoordinateRequest(lot, lat),
                        planTime
                )
                .exceptionally(t -> null)
                .thenApply(weatherInfo -> weatherInfo)
                .join();
    }
}
