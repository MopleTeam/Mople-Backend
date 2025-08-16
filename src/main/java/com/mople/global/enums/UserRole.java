package com.mople.global.enums;

public enum UserRole {
    HOST, CREATOR, PARTICIPANT;

    public static UserRole getRole(Long userId, Long hostId) {
        if (userId.equals(hostId)) {
            return HOST;
        }

        return PARTICIPANT;
    }

    public static UserRole getRole(Long userId, Long hostId, Long creatorId) {
        if (userId.equals(hostId)) {
            if (userId.equals(creatorId)) {
                return CREATOR;
            }
            return HOST;
        }

        if (userId.equals(creatorId)) {
            return CREATOR;
        }

        return PARTICIPANT;
    }
}
