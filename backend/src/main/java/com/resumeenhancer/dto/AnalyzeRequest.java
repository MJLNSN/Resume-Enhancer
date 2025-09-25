package com.resumeenhancer.dto;

import javax.validation.constraints.NotNull;

public class AnalyzeRequest {
    @NotNull
    private Long resumeId;
    
    private String jobDescription;
    
    @NotNull
    private String mode = "local"; // "local" or "gpt"
    
    private Boolean forceRefresh = false;

    public AnalyzeRequest() {}

    public AnalyzeRequest(Long resumeId, String jobDescription, String mode, Boolean forceRefresh) {
        this.resumeId = resumeId;
        this.jobDescription = jobDescription;
        this.mode = mode;
        this.forceRefresh = forceRefresh;
    }

    // Getters and Setters
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Boolean getForceRefresh() { return forceRefresh; }
    public void setForceRefresh(Boolean forceRefresh) { this.forceRefresh = forceRefresh; }
}
