package com.resumeenhancer.repository;

import com.resumeenhancer.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Job> findByIdAndUserId(Long id, Long userId);
}
