package com.example.multimedia_processing.repository;

import com.example.multimedia_processing.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByIdAndUser_Id(UUID jobId, UUID userId);
}