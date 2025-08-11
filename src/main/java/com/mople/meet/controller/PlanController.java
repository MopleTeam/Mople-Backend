package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.core.annotation.log.BusinessLogicLogging;
import com.mople.dto.client.ParticipantClientResponse;
import com.mople.dto.client.PlanClientResponse;
import com.mople.dto.request.meet.plan.PlanReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.dto.response.meet.plan.*;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.meet.service.PlanService;
import com.mople.dto.request.meet.plan.PlanCreateRequest;
import com.mople.dto.request.meet.plan.PlanUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
@Tag(name = "PLAN", description = "일정 API")
public class PlanController {
    private final PlanService planService;

    @Operation(
            summary = "홈화면 일정 조회 API",
            description = "홈화면에서 보여지는 일정 5개와 모임 리스트를 반환합니다."
    )
    @GetMapping("/view")
    public ResponseEntity<PlanHomeViewResponse> planView(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        return ResponseEntity.ok(planService.getPlanView(user.id()));
    }

    @Operation(
            summary = "일정 생성 API",
            description = "모임에서 일정을 생성하여 일정 정보를 반환합니다."
    )
    @PostMapping("/create")
    public ResponseEntity<PlanClientResponse> createPlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody PlanCreateRequest planCreateRequest
    ) {
        return ResponseEntity.ok(planService.createPlan(user.id(), planCreateRequest));
    }

    @Operation(
            summary = "일정 수정 API",
            description = "일정 생성자는 일정을 수정할 수 있으며, 수정된 정보를 반환합니다."
    )
    @BusinessLogicLogging
    @PatchMapping("/update")
    public ResponseEntity<PlanClientResponse> updatePlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody @Valid PlanUpdateRequest planUpdateRequest
    ) {
        return ResponseEntity.ok(planService.updatePlan(user.id(), planUpdateRequest));
    }

    @Operation(
            summary = "일정 삭제 API",
            description = "일정 생성자는 일정을 삭제할 수 있습니다."
    )
    @DeleteMapping("/{planId}")
    public ResponseEntity<PlanViewResponse> deletePlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long planId
    ) {
        planService.deletePlan(user.id(), planId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "일정 조회 API",
            description = "모임에 참가한 유저는 일정을 조회할 수 있습니다."
    )
    @GetMapping("/detail/{planId}")
    public ResponseEntity<PlanClientResponse> getMeetingPlanDetail(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(planService.getPlanDetail(user.id(), planId));
    }

    @Operation(
            summary = "일정 목록 조회 API",
            description = "조회 일을 기준으로 모임의 일정 목록을 반환합니다."
    )
    @GetMapping("/list/{meetId}")
    public ResponseEntity<FlatCursorPageResponse<PlanClientResponse>> getPlans(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(planService.getPlanList(user.id(), meetId, request));
    }

    @Operation(
            summary = "전체 날짜 조회 API",
            description = "유저의 일정과 후기가 존재하는 날짜 리스트 API 입니다."
    )
    @GetMapping("/date")
    public ResponseEntity<UserAllDateResponse> getPlanPages(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        return ResponseEntity.ok(planService.getAllDates(user.id()));
    }

    @Operation(
            summary = "일정 페이징 API",
            description = "달력에서 표시되는 페이징 API 입니다."
    )
    @GetMapping("/page")
    public ResponseEntity<UserPageResponse> getPlanPages(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestParam @DateTimeFormat(pattern = "yyyyMM") YearMonth date
    ) {
        return ResponseEntity.ok(planService.getPlanPages(user.id(), date));
    }

    @Operation(
            summary = "일정 신고 API",
            description = "유저가 신고한 일정을 저장하고 Admin Page에서 조회합니다."
    )
    @PostMapping("/report")
    public ResponseEntity<Void> reportPlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody PlanReportRequest planReportRequest
    ) {
        planService.reportPlan(user.id(), planReportRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "일정 참가 유저 조회 API",
            description = "일정에 참가하는 유저 정보를 반환합니다."
    )
    @GetMapping("/participants/{planId}")
    public ResponseEntity<FlatCursorPageResponse<ParticipantClientResponse>> getParticipants(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long planId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(planService.getParticipantList(user.id(), planId, request));
    }

    @Operation(
            summary = "유저 일정 참가 API",
            description = "일정에 참가하는 유저 정보를 DB에 저장합니다."
    )
    @PostMapping("/join/{planId}")
    public ResponseEntity<Void> joinPlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long planId
    ) {
        planService.joinPlanParticipant(user.id(), planId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "유저 일정 나가기 API",
            description = "일정에 참가하고 있는 유저 정보를 DB에서 삭제합니다."
    )
    @DeleteMapping("/leave/{planId}")
    public ResponseEntity<Void> leavePlan(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long planId
    ) {
        planService.deletePlanParticipant(user.id(), planId);
        return ResponseEntity.ok().build();
    }
}