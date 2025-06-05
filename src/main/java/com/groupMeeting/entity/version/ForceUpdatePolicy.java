package com.groupMeeting.entity.version;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "force_update_policy")
@Getter
public class ForceUpdatePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "force_update_policy_id")
    private Long id;

    @Setter
    @Column(name = "os", unique = true, nullable = false)
    private String os;

    @Setter
    @Column(name = "min_version", nullable = false)
    private int minVersion;

    @Setter
    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    @Setter
    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate;
}
