package com.resumeenhancer.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ExportRequest {
    @NotNull
    private Long enhancedResumeId;
    
    @NotNull
    @Pattern(regexp = "markdown|pdf", message = "Format must be 'markdown' or 'pdf'")
    private String format;
    
    private String filename;

    public ExportRequest() {}

    public ExportRequest(Long enhancedResumeId, String format, String filename) {
        this.enhancedResumeId = enhancedResumeId;
        this.format = format;
        this.filename = filename;
    }

    // Getters and Setters
    public Long getEnhancedResumeId() { return enhancedResumeId; }
    public void setEnhancedResumeId(Long enhancedResumeId) { this.enhancedResumeId = enhancedResumeId; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
