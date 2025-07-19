package com.mople.dto.response.admin;

import com.mople.entity.meet.plan.PlanReport;

public record AdminPlanResponse(
        Long id,
        String reason,
        Long userId,
        Long planId
) {

    public AdminPlanResponse(PlanReport planReport){
        this(
                planReport.getId(),
                planReport.getReason(),
                planReport.getReporterId(),
                planReport.getPlanId()
        );
    }
}
