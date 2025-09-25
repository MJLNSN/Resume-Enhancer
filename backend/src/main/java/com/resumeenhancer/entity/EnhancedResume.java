package com.resumeenhancer.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "enhanced_resumes")
public class EnhancedResume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "enhanced_text", columnDefinition = "TEXT")
    private String enhancedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language = Language.ORIGINAL;

    @Type(type = "io.hypersistence.utils.hibernate.type.json.JsonType")
    @Column(name = "suggestions", columnDefinition = "jsonb")
    private Object suggestions;

    @Column(name = "enhancement_type")
    private String enhancementType; // "rewrite", "translate", "suggest"

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Language {
        ORIGINAL, EN, ZH
    }

    public EnhancedResume() {}

    public EnhancedResume(Resume resume, String enhancedText, Language language, String enhancementType) {
        this.resume = resume;
        this.enhancedText = enhancedText;
        this.language = language;
        this.enhancementType = enhancementType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getEnhancedText() { return enhancedText; }
    public void setEnhancedText(String enhancedText) { this.enhancedText = enhancedText; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public Object getSuggestions() { return suggestions; }
    public void setSuggestions(Object suggestions) { this.suggestions = suggestions; }

    public String getEnhancementType() { return enhancementType; }
    public void setEnhancementType(String enhancementType) { this.enhancementType = enhancementType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
