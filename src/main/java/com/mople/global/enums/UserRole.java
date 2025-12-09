package com.mople.global.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    HOST(1),
    CREATOR(2),
    PARTICIPANT(3);

    private final int order;

    UserRole(int order) {
        this.order = order;
    }

    public static UserRole fromOrder(int roleOrder) {
        return switch (roleOrder) {
            case 1 -> HOST;
            case 2 -> CREATOR;
            default -> PARTICIPANT;
        };
    }
}
