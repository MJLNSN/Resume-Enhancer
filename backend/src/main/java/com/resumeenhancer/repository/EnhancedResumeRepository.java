package com.resumeenhancer.repository;

import com.resumeenhancer.entity.EnhancedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnhancedResumeRepository extends JpaRepository<EnhancedResume, Long> {
    List<EnhancedResume> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    Optional<EnhancedResume> findByIdAndResumeUserId(Long id, Long userId);
}
