package com.mople.entity.notification;

import com.mople.entity.common.BaseTimeEntity;

import jakarta.persistence.*;

import lombok.AccessLevel;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "firebase_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "device_id")
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "active")
    private boolean active;

    @Builder
    public FirebaseToken(String token, String deviceToken, Long userId) {
        this.token = token;
        this.deviceToken = deviceToken;
        this.userId = userId;
        this.active = true;
    }

    public void updateToken(String token){
        this.token = token;
    }

    public void activeToken(){
        this.active = true;
    }

    public void inActiveToken(){
        this.active = false;
    }
}
