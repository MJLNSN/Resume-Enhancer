package com.resumeenhancer.controller;

import com.resumeenhancer.security.UserPrincipal;
import com.resumeenhancer.service.UsageTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usage")
public class UsageController {

    @Autowired(required = false)
    private UsageTrackingService usageTrackingService;

    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getUserUsageLimits(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        if (usageTrackingService == null) {
            return ResponseEntity.ok(Map.of(
                "gptRemaining", -1,
                "enhancementRemaining", -1,
                "unlimited", true
            ));
        }

        int gptRemaining = usageTrackingService.getRemainingGptUsage(currentUser.getId());
        int enhancementRemaining = usageTrackingService.getRemainingEnhancementUsage(currentUser.getId());

        return ResponseEntity.ok(Map.of(
            "gptRemaining", gptRemaining,
            "enhancementRemaining", enhancementRemaining,
            "unlimited", false,
            "canUseGpt", usageTrackingService.canUseGptService(currentUser.getId()),
            "canUseEnhancement", usageTrackingService.canUseEnhancement(currentUser.getId())
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetUsage(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        if (usageTrackingService == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Usage tracking is not enabled"));
        }

        usageTrackingService.resetDailyUsage(currentUser.getId());
        
        return ResponseEntity.ok(Map.of(
            "message", "Daily usage limits have been reset"
        ));
    }
}
