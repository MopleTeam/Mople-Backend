package com.groupMeeting.version.controller;

import com.groupMeeting.dto.client.ForceUpdatePolicyClientResponse;
import com.groupMeeting.version.service.ForceUpdatePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/policy")
@Tag(name = "POLICY", description = "강제 업데이트 API")
public class ForceUpdatePolicyController {
    private final ForceUpdatePolicyService forceUpdatePolicyService;

    @Operation(
            summary = "강제 업데이트 확인 API",
            description = "강제 업데이트가 필요한 버전인지 확인합니다."
    )
    @GetMapping("/force-update/status")
    public ResponseEntity<ForceUpdatePolicyClientResponse> checkForceUpdateStatus(
            @RequestHeader("os") String os,
            @RequestHeader("version") String version
    ) {
        return ResponseEntity.ok(forceUpdatePolicyService.getForceUpdatePolicy(os, version));
    }
}
