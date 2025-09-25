package com.resumeenhancer.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class TranslateRequest {
    private Long resumeId;
    
    private String text;
    
    @NotNull
    @Pattern(regexp = "en|zh|es|fr|de|ja|ko", message = "Target language must be one of: en, zh, es, fr, de, ja, ko")
    private String targetLang;
    
    @NotNull
    @Pattern(regexp = "gpt|local", message = "Mode must be 'gpt' or 'local'")
    private String mode = "gpt";

    public TranslateRequest() {}

    public TranslateRequest(Long resumeId, String text, String targetLang, String mode) {
        this.resumeId = resumeId;
        this.text = text;
        this.targetLang = targetLang;
        this.mode = mode;
    }

    // Getters and Setters
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
