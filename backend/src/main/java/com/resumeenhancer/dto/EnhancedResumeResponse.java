package com.resumeenhancer.dto;

import com.resumeenhancer.entity.EnhancedResume;

import java.time.LocalDateTime;
import java.util.List;

public class EnhancedResumeResponse {
    private Long id;
    private Long resumeId;
    private String enhancedText;
    private EnhancedResume.Language language;
    private List<String> suggestions;
    private String enhancementType;
    private LocalDateTime createdAt;

    public EnhancedResumeResponse() {}

    public EnhancedResumeResponse(Long id, Long resumeId, String enhancedText, 
                                 EnhancedResume.Language language, List<String> suggestions,
                                 String enhancementType, LocalDateTime createdAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.enhancedText = enhancedText;
        this.language = language;
        this.suggestions = suggestions;
        this.enhancementType = enhancementType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getEnhancedText() { return enhancedText; }
    public void setEnhancedText(String enhancedText) { this.enhancedText = enhancedText; }

    public EnhancedResume.Language getLanguage() { return language; }
    public void setLanguage(EnhancedResume.Language language) { this.language = language; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getEnhancementType() { return enhancementType; }
    public void setEnhancementType(String enhancementType) { this.enhancementType = enhancementType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
