package com.example.multimedia_processing.messaging;

import com.example.multimedia_processing.dto.JobResultMessage;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobResultListener {

    private final JobService jobService;

    @RabbitListener(queues = "${rabbitmq.queue.results}")
    public void receiveResult(JobResultMessage result) {

        UUID jobId = UUID.fromString(result.getJobId());

        JobStatus status = JobStatus.valueOf(result.getStatus());
        switch (status) {
            case PROCESSING -> {
                jobService.updateProcessing(jobId, result.getProgressPercentage());
                System.out.println("Job processing: " + jobId + " " + result.getProgressPercentage() + "%");
            }

            case DONE -> {
                jobService.completeJob(jobId, result.getResultFileKey());
                System.out.println("Job completed: " + jobId);
            }

            case FAILED -> {
                jobService.failJob(jobId);
                System.out.println("Job failed: " + jobId);
            }

            default -> {}
        }
    }
}