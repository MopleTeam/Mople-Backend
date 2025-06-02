package com.groupMeeting.entity.user;

import com.groupMeeting.global.enums.Role;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "admin_id")
    private Long id;

    @Column(name = "id", length = 50)
    private String name;

    @Column(name = "pw", length = 20)
    private String pw;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10, updatable = false)
    private Role role;

    @Builder
    public Admin(
            String name,
            String pw,
            Role role
    ) {
        this.name = name;
        this.pw = pw;
        this.role = role;
    }
}