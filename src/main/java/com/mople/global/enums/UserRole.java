package com.mople.global.enums;

public enum UserRole {
    HOST, CREATOR, PARTICIPANT;

    public static UserRole getRole(Integer roleOrder) {
        if (roleOrder.equals(1)) {
            return HOST;
        }

        if (roleOrder.equals(2)) {
            return CREATOR;
        }

        return PARTICIPANT;
    }
}
