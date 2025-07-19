package com.mople.policy.controller;

import com.mople.dto.client.ForceUpdatePolicyClientResponse;
import com.mople.policy.service.ForceUpdatePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;

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
            @Nullable @RequestHeader("os") String os,
            @Nullable @RequestHeader("version") String version
    ) {
        return ResponseEntity.ok(forceUpdatePolicyService.getForceUpdatePolicy(os, version));
    }
}
