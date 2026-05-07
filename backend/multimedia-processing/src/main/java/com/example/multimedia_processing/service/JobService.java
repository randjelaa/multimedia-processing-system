package com.example.multimedia_processing.service;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Job createJob(String originalFileName,
                         String objectKey) {

        Job job = new Job();

        job.setOriginalFileName(originalFileName);
        job.setObjectKey(objectKey);

        job.setStatus(JobStatus.PENDING);

        job.setCreatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }
}
