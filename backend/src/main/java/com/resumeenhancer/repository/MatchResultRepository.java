package com.resumeenhancer.repository;

import com.resumeenhancer.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByResumeIdOrderByScoreDesc(Long resumeId);
    List<MatchResult> findByJobIdOrderByScoreDesc(Long jobId);
}
