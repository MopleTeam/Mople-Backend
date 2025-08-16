package com.mople.entity.policy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_version_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiVersionPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "api_version_policy_id")
    private Long id;

    @Column(name = "os", nullable = false)
    private String os;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "app_version", nullable = false)
    private int appVersion;

    @Column(name = "api_version", nullable = false)
    private String apiVersion;

    @Column(name = "description")
    private String description;

    @Builder
    public ApiVersionPolicy(String os, String uri, int appVersion, String apiVersion, String description) {
        this.os = os;
        this.uri = uri;
        this.appVersion = appVersion;
        this.apiVersion = apiVersion;
        this.description = description;
    }

    public void updatePolicy(String os, String uri, int appVersion, String apiVersion, String description) {
        this.os = os;
        this.uri = uri;
        this.appVersion = appVersion;
        this.apiVersion = apiVersion;
        this.description = description;
    }
}
