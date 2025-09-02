package com.mople.dto.response.meet.plan;

import com.mople.entity.meet.plan.MeetPlan;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PlanViewResponse(
        Long planId,
        Long version,
        Long meetId,
        String meetName,
        String meetImage,
        String planName,
        Long creatorId,
        Integer planMemberCount,
        LocalDateTime planTime,
        String planAddress,
        String title,
        BigDecimal lat,
        BigDecimal lot,
        String weatherIcon,
        String weatherAddress,
        Double temperature,
        Double pop
) {
    public static PlanViewResponse ofPlanView(MeetPlan plan, String meetName, String meetImage, Integer planMemberCount) {
        return PlanViewResponse.builder()
                .planId(plan.getId())
                .version(plan.getVersion())
                .meetId(plan.getMeetId())
                .meetName(meetName)
                .meetImage(meetImage)
                .planName(plan.getName())
                .creatorId(plan.getCreatorId())
                .planMemberCount(planMemberCount)
                .planTime(plan.getPlanTime())
                .planAddress(plan.getAddress())
                .title(plan.getTitle())
                .lat(plan.getLatitude())
                .lot(plan.getLongitude())
                .weatherIcon(plan.getWeatherIcon())
                .weatherAddress(plan.getWeatherAddress())
                .temperature(plan.getTemperature())
                .pop(plan.getPop())
                .build();
    }
}
