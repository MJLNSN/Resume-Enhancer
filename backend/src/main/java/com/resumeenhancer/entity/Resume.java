package com.resumeenhancer.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Type(type = "io.hypersistence.utils.hibernate.type.json.JsonType")
    @Column(name = "parsed_json", columnDefinition = "jsonb")
    private Object parsedJson;

    @Column(name = "parse_error")
    private Boolean parseError = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EnhancedResume> enhancedVersions = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchResult> matchResults = new ArrayList<>();

    public Resume() {}

    public Resume(User user, String fileUrl) {
        this.user = user;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public List<EnhancedResume> getEnhancedVersions() { return enhancedVersions; }
    public void setEnhancedVersions(List<EnhancedResume> enhancedVersions) { this.enhancedVersions = enhancedVersions; }

    public List<MatchResult> getMatchResults() { return matchResults; }
    public void setMatchResults(List<MatchResult> matchResults) { this.matchResults = matchResults; }
}
