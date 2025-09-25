package com.resumeenhancer.controller;

import com.resumeenhancer.dto.ResumeResponse;
import com.resumeenhancer.dto.TextResumeRequest;
import com.resumeenhancer.security.UserPrincipal;
import com.resumeenhancer.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            ResumeResponse response = resumeService.uploadResume(file, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("File upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/text")
    public ResponseEntity<?> createTextResume(
            @Valid @RequestBody TextResumeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            ResumeResponse response = resumeService.createTextResume(request, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Text resume creation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            ResumeResponse response = resumeService.getResume(id, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getUserResumes(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ResumeResponse> resumes = resumeService.getUserResumes(currentUser.getId());
        return ResponseEntity.ok(resumes);
    }
}
