package com.groupMeeting.meet.controller;

import com.groupMeeting.meet.service.MeetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invite")
@RequiredArgsConstructor
@Tag(name = "INVITE", description = "초대 API")
public class InviteController {
    private final MeetService meetService;

    @Operation(
            summary = "초대 URL REDIRECT API",
            description = "초대 url을 링크 전환 페이지로 이동 시킵니다."
    )
    @GetMapping
    public String redirectInvitePage(@RequestParam String code, Model model) {
        meetService.inviteMeetInfo(code, model);
        return "deepLink";
    }
}