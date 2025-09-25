package com.resumeenhancer.controller;

import com.resumeenhancer.dto.ExportRequest;
import com.resumeenhancer.dto.ExportResponse;
import com.resumeenhancer.entity.EnhancedResume;
import com.resumeenhancer.repository.EnhancedResumeRepository;
import com.resumeenhancer.security.UserPrincipal;
import com.resumeenhancer.service.ExportService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private EnhancedResumeRepository enhancedResumeRepository;

    @PostMapping
    public ResponseEntity<ExportResponse> exportResume(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Get enhanced resume
            EnhancedResume enhanced = enhancedResumeRepository.findByIdAndResumeUserId(
                    request.getEnhancedResumeId(), currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Enhanced resume not found"));

            String filename = request.getFilename();
            if (filename == null || filename.trim().isEmpty()) {
                filename = exportService.generateUniqueFilename("resume", currentUser.getId());
            }

            if ("markdown".equals(request.getFormat())) {
                // Export to Markdown
                String markdown = exportService.exportToMarkdown(
                    enhanced.getEnhancedText(), 
                    "Resume #" + enhanced.getResume().getId());
                
                // Return markdown content directly in response
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + filename + ".md\"")
                        .header(HttpHeaders.CONTENT_TYPE, "text/markdown")
                        .body(new ExportResponse(null, "markdown", filename + ".md", 
                               (long) markdown.getBytes().length));
                        
            } else if ("pdf".equals(request.getFormat())) {
                // Export to PDF
                String downloadUrl = exportService.exportToPdfUrl(enhanced.getEnhancedText(), filename);
                
                return ResponseEntity.ok(new ExportResponse(downloadUrl, "pdf", filename + ".pdf", null));
                
            } else {
                return ResponseEntity.badRequest().build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/markdown/{enhancedResumeId}")
    public ResponseEntity<String> exportMarkdownDirect(
            @PathVariable Long enhancedResumeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResume enhanced = enhancedResumeRepository.findByIdAndResumeUserId(
                    enhancedResumeId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Enhanced resume not found"));

            String markdown = exportService.exportToMarkdown(
                enhanced.getEnhancedText(), 
                "Resume #" + enhanced.getResume().getId());

            String filename = exportService.generateUniqueFilename("resume", currentUser.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + ".md\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(markdown);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to export markdown");
        }
    }

    @GetMapping("/compare/{enhancedResumeId}")
    public ResponseEntity<String> exportComparison(
            @PathVariable Long enhancedResumeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            EnhancedResume enhanced = enhancedResumeRepository.findByIdAndResumeUserId(
                    enhancedResumeId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Enhanced resume not found"));

            String originalText = enhanced.getResume().getRawText();
            String enhancedText = enhanced.getEnhancedText();

            if (originalText == null || originalText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Original text not available");
            }

            String comparisonHtml = exportService.createVersionComparisonHtml(
                originalText, enhancedText, "Original", "Enhanced");

            String filename = exportService.generateUniqueFilename("comparison", currentUser.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + ".html\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(comparisonHtml);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create comparison");
        }
    }
}
