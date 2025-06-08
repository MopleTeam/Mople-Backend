package com.groupMeeting.entity.version;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "force_update_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForceUpdatePolicy {
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

    @Builder
    public ForceUpdatePolicy(String os, int minVersion, int currentVersion, boolean forceUpdate) {
        this.os = os;
        this.minVersion = minVersion;
        this.currentVersion = currentVersion;
        this.forceUpdate = forceUpdate;
    }

    public void updatePolicy(String os, int minVersion, int currentVersion, boolean forceUpdate) {
        this.os = os;
        this.minVersion = minVersion;
        this.currentVersion = currentVersion;
        this.forceUpdate = forceUpdate;
    }
}
