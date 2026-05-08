package com.example.multimedia_processing.service;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.entity.User;
import com.example.multimedia_processing.repository.JobRepository;
import com.example.multimedia_processing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Job createJob(
            String originalFileName,
            String objectKey,
            UUID userId
    ) {

        Job job = new Job();
        job.setOriginalFileName(originalFileName);
        job.setObjectKey(objectKey);
        job.setStatus(JobStatus.PENDING);
        job.setCreatedAt(LocalDateTime.now());

        User user = userRepository.getReferenceById(userId);
        job.setUser(user);

        return jobRepository.save(job);
    }
}
