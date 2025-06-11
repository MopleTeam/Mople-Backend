package com.groupMeeting.entity.policy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "force_update_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForceUpdatePolicy {
    private static final boolean DEFAULT_REQUIRED_FORCE_UPDATE = true;
    private static final String DEFAULT_REQUIRED_MIN_VERSION = null;
    private static final String DEFAULT_REQUIRED_MESSAGE = "보다 좋은 서비스를 위해 최신 버전으로 업데이트가 필요합니다.";
    private static final String NO_REQUIRED_MESSAGE = "해당 사항 없음";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "force_update_policy_id")
    private Long id;

    @Column(name = "os", unique = true, nullable = false)
    private String os;

    @Column(name = "min_version", nullable = false)
    private int minVersion;

    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate;

    @Column(name = "message", nullable = false)
    private String message;

    @Builder
    public ForceUpdatePolicy(String os, int minVersion, int currentVersion, boolean forceUpdate, String message) {
        this.os = os;
        this.minVersion = minVersion;
        this.currentVersion = currentVersion;
        this.forceUpdate = forceUpdate;
        this.message = message;
    }

    public void updatePolicy(String os, int minVersion, int currentVersion, boolean forceUpdate, String message) {
        this.os = os;
        this.minVersion = minVersion;
        this.currentVersion = currentVersion;
        this.forceUpdate = forceUpdate;
        this.message = message;
    }

    public boolean isForceUpdateRequired(int userVersion) {
        return userVersion < minVersion && forceUpdate;
    }

    public String getForceUpdateMessage(int userVersion) {
        return isForceUpdateRequired(userVersion) ? message : NO_REQUIRED_MESSAGE;
    }

    public static boolean getDefaultForceUpdate() {
        return DEFAULT_REQUIRED_FORCE_UPDATE;
    }

    public static String getDefaultMinVersion() {
        return DEFAULT_REQUIRED_MIN_VERSION;
    }

    public static String getDefaultMessage() {
        return DEFAULT_REQUIRED_MESSAGE;
    }
}
