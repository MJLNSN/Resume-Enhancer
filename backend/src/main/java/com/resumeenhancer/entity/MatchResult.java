package com.resumeenhancer.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_results")
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "score")
    private Float score;

    @Type(type = "io.hypersistence.utils.hibernate.type.json.JsonType")
    @Column(name = "matched_skills", columnDefinition = "jsonb")
    private Object matchedSkills;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MatchResult() {}

    public MatchResult(Resume resume, Job job, Float score, Object matchedSkills) {
        this.resume = resume;
        this.job = job;
        this.score = score;
        this.matchedSkills = matchedSkills;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }

    public Object getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(Object matchedSkills) { this.matchedSkills = matchedSkills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
