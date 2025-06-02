package com.groupMeeting.global.enums;

public enum Role {
    ADMIN, USER, BLACK;

    public String securityRole(){
        return "ROLE_" + this.name();
    }
}
