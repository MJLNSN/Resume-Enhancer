package com.resumeenhancer.controller;

import com.resumeenhancer.security.UserPrincipal;
import com.resumeenhancer.service.UsageTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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
                "unlimited", true,
                "resetTimeUTC", "",
                "hoursUntilReset", 0
            ));
        }

        int gptRemaining = usageTrackingService.getRemainingGptUsage(currentUser.getId());
        int enhancementRemaining = usageTrackingService.getRemainingEnhancementUsage(currentUser.getId());
        
        // Calculate time until midnight UTC (when limits reset)
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime nextMidnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long hoursUntilReset = ChronoUnit.HOURS.between(now, nextMidnight);
        long minutesUntilReset = ChronoUnit.MINUTES.between(now, nextMidnight) % 60;
        
        String resetTimeFormatted = String.format("%02d:%02d", hoursUntilReset, minutesUntilReset);

        return ResponseEntity.ok(Map.of(
            "gptRemaining", gptRemaining,
            "enhancementRemaining", enhancementRemaining,
            "unlimited", false,
            "canUseGpt", usageTrackingService.canUseGptService(currentUser.getId()),
            "canUseEnhancement", usageTrackingService.canUseEnhancement(currentUser.getId()),
            "resetTimeUTC", nextMidnight.toString(),
            "hoursUntilReset", hoursUntilReset,
            "resetTimeFormatted", resetTimeFormatted
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
