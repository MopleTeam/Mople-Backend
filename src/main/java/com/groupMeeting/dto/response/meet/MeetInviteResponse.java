package com.groupMeeting.dto.response.meet;

import java.time.LocalDateTime;

public record MeetInviteResponse(LocalDateTime expiredAt, String inviteUrl) {
}
