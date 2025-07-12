package com.groupMeeting.notification.controller;

import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.dto.request.notification.topic.PushTopicRequest;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.response.notification.NotificationListResponse;
import com.groupMeeting.global.enums.PushTopic;
import com.groupMeeting.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Tag(name = "NOTIFICATION", description = "알림 API")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @Operation(
            summary = "Notify Topic 조회 API",
            description = "Notify Topic을 조회합니다."
    )
    @GetMapping("/subscribe")
    public ResponseEntity<List<PushTopic>> subscribeTopic(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        return ResponseEntity.ok(service.getSubscribeList(user.id()));
    }

    @Operation(
            summary = "Notify Topic 구독 API",
            description = "Notify Topic을 저장합니다."
    )
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribeTopic(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody PushTopicRequest pushTopicRequest
    ) {
        service.subscribeNotifyTopic(user.id(), pushTopicRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Notify Topic 구독 해제 API",
            description = "Notify Topic을 삭제합니다."
    )
    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribeTopic(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody PushTopicRequest pushTopicRequest
    ) {
        service.unsubscribeNotifyTopic(user.id(), pushTopicRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Notify 조회 API",
            description = "알림 항목을 조회합니다."
    )
    @GetMapping("/list")
    public ResponseEntity<List<NotificationListResponse>> notificationList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        return ResponseEntity.ok(service.getUserNotificationList(user.id()));
    }

    @Operation(
            summary = "Notify Count 초기화 API",
            description = "유저의 알림 카운트를 초기화합니다."
    )
    @PutMapping("/clear")
    public ResponseEntity<Void> badgeClearCount(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        service.userBadgeCountClear(user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Notify Count 차감 API",
            description = "알림을 조회했을 때 알림 카운트를 1 줄입니다."
    )
    @PutMapping("/minus/{notificationId}")
    public ResponseEntity<Void> badgeMinusCount(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long notificationId
    ) {
        service.userBadgeCountMinus(user.id(), notificationId);
        return ResponseEntity.ok().build();
    }
}
