package com.resumeenhancer.dto;

import java.time.LocalDateTime;

public class ResumeResponse {
    private Long id;
    private String fileUrl;
    private String rawText;
    private Object parsedJson;
    private Boolean parseError;
    private LocalDateTime createdAt;

    public ResumeResponse() {}

    public ResumeResponse(Long id, String fileUrl, String rawText, Object parsedJson, 
                         Boolean parseError, LocalDateTime createdAt) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.rawText = rawText;
        this.parsedJson = parsedJson;
        this.parseError = parseError;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public Object getParsedJson() { return parsedJson; }
    public void setParsedJson(Object parsedJson) { this.parsedJson = parsedJson; }

    public Boolean getParseError() { return parseError; }
    public void setParseError(Boolean parseError) { this.parseError = parseError; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
