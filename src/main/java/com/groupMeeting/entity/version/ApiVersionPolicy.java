package com.groupMeeting.entity.version;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "api_version_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiVersionPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_version_policy_id")
    private Long id;

    @Setter
    @Column(name = "os", nullable = false)
    private String os;

    @Setter
    @Column(name = "uri", nullable = false)
    private String uri;

    @Setter
    @Column(name = "app_version", nullable = false)
    private int appVersion;

    @Setter
    @Column(name = "api_version", nullable = false)
    private String apiVersion;

    @Setter
    @Column(name = "description")
    private String description;
}
