package com.groupMeeting.entity.version;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "force_update_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForceUpdatePolicy {
    private static final String DEFAULT_MESSAGE = "해당 사항 없음";

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

    public String getUpdateMessage(int userVersion) {
        return isForceUpdateRequired(userVersion) ? message : DEFAULT_MESSAGE;
    }
}
