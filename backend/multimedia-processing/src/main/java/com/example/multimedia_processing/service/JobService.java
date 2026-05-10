package com.example.multimedia_processing.service;

import com.example.multimedia_processing.dto.JobMessage;
import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.entity.JobType;
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
    private final QueueProducer queueProducer;

    public Job createJob(
            String originalFileName,
            String objectKey,
            UUID userId,
            String type
    ) {

        Job job = new Job();
        job.setOriginalFileName(originalFileName);
        job.setObjectKey(objectKey);
        job.setStatus(JobStatus.PENDING);
        job.setCreatedAt(LocalDateTime.now());
        job.setType(JobType.valueOf(type));

        User user = userRepository.getReferenceById(userId);
        job.setUser(user);
        Job savedJob = jobRepository.save(job);

        JobMessage message =
                new JobMessage(
                        savedJob.getId().toString(),
                        objectKey,
                        type
                );

        queueProducer.sendJob(
                message,
                job.getType()
        );

        return savedJob;
    }

    public void completeJob(
            UUID jobId,
            String resultFileKey
    ) {

        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.DONE);
        job.setResultFileKey(resultFileKey);
        job.setFinishedAt(LocalDateTime.now());
        job.setProgressPercentage(100);

        jobRepository.save(job);
    }

    public void failJob(UUID jobId) {

        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.FAILED);
        job.setFinishedAt(LocalDateTime.now());
        job.setProgressPercentage(0);

        jobRepository.save(job);
    }

    public void updateProcessing(
            UUID jobId,
            Integer progressPercentage
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow();

        job.setStatus(JobStatus.PROCESSING);

        if (progressPercentage != null) {
            job.setProgressPercentage(progressPercentage);
        }

        jobRepository.save(job);
    }
}
