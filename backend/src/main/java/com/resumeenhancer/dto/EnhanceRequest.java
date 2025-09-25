package com.resumeenhancer.dto;

import javax.validation.constraints.NotNull;

public class EnhanceRequest {
    @NotNull
    private Long resumeId;
    
    private String jobDescription;
    
    @NotNull
    private String mode; // "gpt" or "local"

    public EnhanceRequest() {}

    public EnhanceRequest(Long resumeId, String jobDescription, String mode) {
        this.resumeId = resumeId;
        this.jobDescription = jobDescription;
        this.mode = mode;
    }

    // Getters and Setters
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
