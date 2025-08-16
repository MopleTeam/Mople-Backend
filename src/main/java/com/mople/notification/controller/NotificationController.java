package com.mople.notification.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.request.notification.topic.PushTopicRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.notification.NotificationResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.global.enums.PushTopic;
import com.mople.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
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
    public ResponseEntity<FlatCursorPageResponse<NotificationResponse>> notificationList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(service.getUserNotificationList(user.id(), request));
    }

    @Operation(
            summary = "모든 Notify 읽음 처리 API",
            description = "모든 알림을 읽음 처리합니다."
    )
    @PutMapping("/clear")
    public ResponseEntity<Void> readAllNotifications(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        service.readAllNotifications(user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "특정 Notify 읽음 처리 API",
            description = "특정 알림을 읽음 처리합니다."
    )
    @PutMapping("/read/{notificationId}")
    public ResponseEntity<Void> readSingleNotification(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long notificationId
    ) {
        service.readSingleNotification(user.id(), notificationId);
        return ResponseEntity.ok().build();
    }
}
