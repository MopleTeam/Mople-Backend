package com.groupMeeting.dto.response.admin;

import com.groupMeeting.entity.user.Admin;
import com.groupMeeting.global.enums.Role;

public record AdminLoginResponse (
        Long id,
        Role role
){
    public AdminLoginResponse(Admin admin){
        this(admin.getId(), admin.getRole());
    }
}
