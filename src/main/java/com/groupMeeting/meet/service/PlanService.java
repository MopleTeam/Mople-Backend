package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.AuthException;
import com.groupMeeting.core.exception.custom.BadRequestException;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.client.PlanClientResponse;
import com.groupMeeting.dto.request.meet.plan.PlanReportRequest;
import com.groupMeeting.dto.request.weather.CoordinateRequest;
import com.groupMeeting.dto.response.meet.UserAllDateResponse;
import com.groupMeeting.dto.response.meet.UserPageResponse;
import com.groupMeeting.dto.response.meet.plan.*;
import com.groupMeeting.dto.response.weather.WeatherInfoResponse;
import com.groupMeeting.entity.meet.MeetTime;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.plan.PlanParticipant;
import com.groupMeeting.entity.meet.plan.PlanReport;
import com.groupMeeting.global.event.data.notify.NotifyEventPublisher;
import com.groupMeeting.meet.mapper.PlanMapper;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.MeetTimeRepository;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.impl.plan.PlanRepositorySupport;
import com.groupMeeting.meet.repository.plan.PlanParticipantRepository;
import com.groupMeeting.dto.request.meet.plan.PlanCreateRequest;
import com.groupMeeting.dto.request.meet.plan.PlanUpdateRequest;
import com.groupMeeting.meet.repository.plan.PlanReportRepository;
import com.groupMeeting.meet.schedule.PlanScheduleJob;
import com.groupMeeting.weather.service.WeatherService;

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
import java.util.Map;

import static com.groupMeeting.dto.client.PlanClientResponse.*;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
    private final WeatherService weatherService;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReportRepository planReportRepository;
    private final PlanParticipantRepository planParticipantRepository;
    private final PlanRepositorySupport planRepositorySupport;
    private final MeetTimeRepository timeRepository;

    private final PlanMapper mapper;
    private final EntityReader reader;

    private final TaskScheduler taskScheduler;
    private final PlanScheduleJob planScheduleJob;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public PlanHomeViewResponse getPlanView(Long userId) {
        return new PlanHomeViewResponse(ofViews(planRepositorySupport.findHomeViewPlan(userId)), reader.findMeetListUseMember(userId));
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
                        Map.of(
                                "planId", plan.getId().toString(),
                                "meetId", meet.getId().toString(),
                                "meetName", meet.getName(),
                                "planName", plan.getName(),
                                "planTime", plan.getPlanTime().toString(),
                                "userId", creatorId.toString()
                        ),
                        Map.of(
                                "planId", plan.getId().toString()
//                                "meetId", meet.getId().toString()
                        )
                )
        );

        if (request.planTime().isAfter(now.plusHours(1))) {
            long hour = now.until(request.planTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;

            taskScheduler.schedule(
                    () -> planScheduleJob.planRemindSchedule(plan.getId(), plan.getPlanTime(), creatorId),
                    plan.getPlanTime().minusHours(hour).atZone(ZoneId.systemDefault()).toInstant()
            );
        }

        return ofView(mapper.getPlanView(plan));
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
                        Map.of(
                                "planId", plan.getId().toString(),
                                "planName", plan.getName(),
                                "meetId", plan.getMeet().getId().toString(),
                                "meetName", plan.getMeet().getName(),
                                "userId", userId.toString()
                        ),
                        Map.of(
                                "planId", plan.getId().toString()
//                                "meetId", plan.getMeet().getId().toString()
                        )
                )
        );
        return ofUpdate(mapper.getPlanView(plan));
    }

    @Transactional
    public void deletePlan(Long userId, Long planId) {
        var plan = reader.findPlan(planId);

        if (plan.isCreator(userId)) {
            throw new BadRequestException(NOT_CREATOR);
        }

        Long meetId = plan.getMeet().getId();

        timeRepository.deleteAllInBatch(timeRepository.findByAllPlanId(planId));

        publisher.publishEvent(
                NotifyEventPublisher.planRemove(
                        Map.of(
                                "planId", planId.toString(),
                                "planName", plan.getName(),
                                "meetId", plan.getMeet().getId().toString(),
                                "meetName", plan.getMeet().getName(),
                                "userId", userId.toString()
                        ),
                        Map.of("meetId", meetId.toString())
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
                planParticipantRepository.existsByPlanIdAndUserId(planId, userId)
        );
    }

    @Transactional(readOnly = true)
    public List<PlanClientResponse> getPlanList(Long userId, Long meetId) {
        return ofLists(planRepositorySupport.findPlanList(userId, meetId));
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
    public PlanParticipantResponse getParticipantList(Long planId) {
        MeetPlan plan = meetPlanRepository.findPlanAll(planId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_PLAN)
        );

        return new PlanParticipantResponse(plan);
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
