package com.mople.dto.client;

import com.mople.dto.response.meet.plan.PlanListResponse;
import com.mople.dto.response.meet.plan.PlanViewResponse;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PlanClientResponse {
    private final Long planId;
    private final Long version;
    private final Long meetId;
    private final String meetName;
    private final String meetImg;
    private final String planName;
    private final String planAddress;
    private final String title;
    private final Long creatorId;
    private final LocalDateTime planTime;
    private final Integer planMemberCount;
    private final BigDecimal lat;
    private final BigDecimal lot;
    private final String weatherAddress;
    private final String weatherIcon;
    private final Double temperature;
    private final Double pop;
    private final boolean participant;
    private final Integer commentCount;

    public static List<PlanClientResponse> ofViews(List<PlanViewResponse> viewResponses) {
        return viewResponses.stream().map(response -> ofView(response, 0)).toList();
    }

    public static PlanClientResponse ofView(PlanViewResponse viewResponse, Integer commentCount) {
        return PlanClientResponse.builder()
                .planId(viewResponse.planId())
                .version(viewResponse.version())
                .meetId(viewResponse.meetId())
                .meetName(viewResponse.meetName())
                .meetImg(viewResponse.meetImage())
                .planName(viewResponse.planName())
                .planMemberCount(viewResponse.planMemberCount())
                .planTime(viewResponse.planTime())
                .planAddress(viewResponse.planAddress())
                .title(viewResponse.title())
                .creatorId(viewResponse.creatorId())
                .lat(viewResponse.lat())
                .lot(viewResponse.lot())
                .weatherIcon(viewResponse.weatherIcon())
                .weatherAddress(viewResponse.weatherAddress())
                .temperature(viewResponse.temperature())
                .pop(viewResponse.pop())
                .commentCount(commentCount)
                .build();
    }

    public static PlanClientResponse ofViewAndParticipant(PlanViewResponse viewResponse, boolean participant, Integer commentCount) {
        return PlanClientResponse.builder()
                .planId(viewResponse.planId())
                .version(viewResponse.version())
                .meetId(viewResponse.meetId())
                .meetName(viewResponse.meetName())
                .meetImg(viewResponse.meetImage())
                .planName(viewResponse.planName())
                .planMemberCount(viewResponse.planMemberCount())
                .planTime(viewResponse.planTime())
                .planAddress(viewResponse.planAddress())
                .title(viewResponse.title())
                .creatorId(viewResponse.creatorId())
                .lat(viewResponse.lat())
                .lot(viewResponse.lot())
                .weatherIcon(viewResponse.weatherIcon())
                .weatherAddress(viewResponse.weatherAddress())
                .temperature(viewResponse.temperature())
                .pop(viewResponse.pop())
                .participant(participant)
                .commentCount(commentCount)
                .build();
    }

    public static PlanClientResponse ofUpdate(PlanViewResponse viewResponse, Integer commentCount) {
        return PlanClientResponse.builder()
                .planId(viewResponse.planId())
                .version(viewResponse.version())
                .meetId(viewResponse.meetId())
                .meetName(viewResponse.meetName())
                .meetImg(viewResponse.meetImage())
                .planName(viewResponse.planName())
                .planMemberCount(viewResponse.planMemberCount())
                .planTime(viewResponse.planTime())
                .planAddress(viewResponse.planAddress())
                .title(viewResponse.title())
                .creatorId(viewResponse.creatorId())
                .lat(viewResponse.lat())
                .lot(viewResponse.lot())
                .weatherIcon(viewResponse.weatherIcon())
                .weatherAddress(viewResponse.weatherAddress())
                .temperature(viewResponse.temperature())
                .pop(viewResponse.pop())
                .participant(true)
                .commentCount(commentCount)
                .build();
    }

    public static List<PlanClientResponse> ofLists(List<PlanListResponse> listResponses) {
        return listResponses.stream().map(PlanClientResponse::ofList).toList();
    }

    private static PlanClientResponse ofList(PlanListResponse listResponse) {
        return PlanClientResponse.builder()
                .planId(listResponse.planId())
                .version(listResponse.version())
                .meetId(listResponse.meetId())
                .meetName(listResponse.meetName())
                .meetImg(listResponse.meetImage())
                .planName(listResponse.planName())
                .creatorId(listResponse.creatorId())
                .planMemberCount(listResponse.planMemberCount())
                .planTime(listResponse.planTime())
                .planAddress(listResponse.planAddress())
                .title(listResponse.title())
                .weatherIcon(listResponse.weatherIcon())
                .weatherAddress(listResponse.weatherAddress())
                .temperature(listResponse.temperature())
                .participant(listResponse.participant())
                .pop(listResponse.pop())
                .build();
    }
}
