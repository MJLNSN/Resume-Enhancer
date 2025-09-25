package com.resumeenhancer.controller;

import com.resumeenhancer.dto.AnalyzeRequest;
import com.resumeenhancer.dto.EnhancedResumeResponse;
import com.resumeenhancer.dto.EnhanceRequest;
import com.resumeenhancer.dto.TranslateRequest;
import com.resumeenhancer.security.UserPrincipal;
import com.resumeenhancer.service.EnhancedResumeService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class AnalysisController {

    @Autowired
    private EnhancedResumeService enhancedResumeService;

    @PostMapping("/analyze")
    public ResponseEntity<EnhancedResumeResponse> analyzeResume(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResumeResponse response = enhancedResumeService.analyzeResume(request, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/enhance")
    public ResponseEntity<?> enhanceResume(
            @Valid @RequestBody EnhanceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResumeResponse response = enhancedResumeService.enhanceResume(request, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Enhancement error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/translate")
    public ResponseEntity<EnhancedResumeResponse> translateResume(
            @Valid @RequestBody TranslateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResumeResponse response = enhancedResumeService.translateResume(request, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enhanced/{id}")
    public ResponseEntity<EnhancedResumeResponse> getEnhancedResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResumeResponse response = enhancedResumeService.getEnhancedResume(id, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resumes/{resumeId}/enhanced")
    public ResponseEntity<List<EnhancedResumeResponse>> getEnhancedVersions(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            List<EnhancedResumeResponse> versions = enhancedResumeService.getEnhancedVersions(resumeId, currentUser.getId());
            return ResponseEntity.ok(versions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
