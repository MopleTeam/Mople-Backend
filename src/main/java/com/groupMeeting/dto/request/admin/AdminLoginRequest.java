package com.groupMeeting.dto.request.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminLoginRequest {
    private String name;
    private String pw;
}
