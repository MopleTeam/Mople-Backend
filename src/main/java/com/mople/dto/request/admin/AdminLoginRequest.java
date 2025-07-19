package com.mople.dto.request.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "pw")
public class AdminLoginRequest {
    private String name;
    private String pw;
}
