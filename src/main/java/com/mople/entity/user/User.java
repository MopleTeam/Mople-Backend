package com.mople.entity.user;

import com.mople.global.enums.Role;
import com.mople.global.enums.SocialProvider;
import com.mople.global.enums.Status;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "email", unique = true, length = 50)
    private String email;

    @Column(name = "nickname", unique = true, length = 20)
    private String nickname;

    @Column(name = "profile_img")
    private String profileImg;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 10)
    private SocialProvider socialProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10, updatable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private Status status;

    @Column(name = "last_launch_at")
    private LocalDateTime lastLaunchAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public User(
            String email,
            String nickname,
            String profileImg,
            LocalDateTime lastLaunchAt,
            Role role,
            Status status,
            SocialProvider socialProvider
    ) {
        this.email = email;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.lastLaunchAt = lastLaunchAt;
        this.role = role;
        this.status = status;
        this.socialProvider = socialProvider;
    }

    public void updateImageAndNickname(String imageName, String nickname) {
        this.profileImg = imageName;
        this.nickname = nickname;
    }

    public boolean imageValid() {
        return profileImg != null && !"null".equals(profileImg);
    }
}