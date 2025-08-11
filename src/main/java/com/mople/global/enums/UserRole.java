package com.mople.global.enums;

public enum UserRole {
    CREATOR, HOST, PARTICIPANT;

    public static UserRole getRole(Long userId, Long creatorId) {
        if (userId.equals(creatorId)) {
            return CREATOR;
        }

        return PARTICIPANT;
    }

    public static UserRole getRole(Long userId, Long creatorId, Long hostId) {
        if (userId.equals(creatorId)) {
            return CREATOR;
        }

        if (userId.equals(hostId)) {
            return HOST;
        }

        return PARTICIPANT;
    }
}
