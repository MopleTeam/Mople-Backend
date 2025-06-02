package com.groupMeeting.notification.mapper;

import com.google.firebase.messaging.*;

import com.groupMeeting.dto.response.notification.NotificationPayload;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
public class TemplateMapper {
    public static NotificationPayload newMeetMember(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 새 멤버 \uD83C\uDF89",
                data.get("userName") + "님이 가입했어요!"
        );
    }

    public static NotificationPayload newPlan(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정등록 \uD83D\uDCC6",
                data.get("planName") + "에 참여해보세요!"
        );
    }

    public static NotificationPayload updatePlan(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정변경",
                data.get("planName") + " 일정이 변경됐어요"
        );
    }

    public static NotificationPayload removePlan(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정취소",
                data.get("planName") + " 일정이 취소됐어요"
        );
    }

    public static NotificationPayload remindPlan(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정 알림 ⏰",
                data.get("planName") + " 곧 시작돼요!"
        );
    }

    public static NotificationPayload remindReview(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정은 어떠셨나요?",
                data.get("reviewName") + "의 사진을 공유해보세요"
        );
    }

    public static NotificationPayload updateReview(Map<String, String> data) {
        return new NotificationPayload(
                data.get("meetName") + "의 일정은 어떠셨나요?",
                data.get("reviewName") + "의 사진을 확인해보세요"
        );
    }

    public static ApnsConfig setNotifyForIOS() {
        return ApnsConfig.builder()
                .setAps(
                        Aps.builder()
                                .setSound("default")
                                .build()
                )
                .build();
    }

    public static AndroidConfig setNotifyForAndroid() {
        return AndroidConfig.builder()
                .setNotification(
                        AndroidNotification.builder()
                                .setSound("default")
                                .build()
                )
                .setPriority(AndroidConfig.Priority.NORMAL)
                .build();
    }

    private static String transTime(String time) {
        return LocalDateTime
                .parse(
                        time,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                )
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
    }
}
