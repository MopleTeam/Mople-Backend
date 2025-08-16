package com.mople.dto.response.admin;

import com.mople.entity.user.Admin;
import com.mople.global.enums.Role;

public record AdminLoginResponse (
        Long id,
        Role role
){
    public AdminLoginResponse(Admin admin){
        this(admin.getId(), admin.getRole());
    }
}
