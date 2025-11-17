package com.mople.meet.controller;

import com.mople.meet.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meet/{meetId}/notices")
@RequiredArgsConstructor
@Tag(name = "NOTICE", description = "공지 API")
public class NoticeController {

    private final NoticeService noticeService;

}
