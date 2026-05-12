package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.security.CustomUserPrincipal;
import com.example.multimedia_processing.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

//    @GetMapping
//    public List<Job> getJobs() {
//        return jobService.getAll();
//    }

    @GetMapping("/{id}")
    public Job getJob(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = UUID.fromString(principal.getUserId());

        return jobService.getJobByIdAndUserId(id, userId);
    }

    @PostMapping("/abort/{jobId}")
    public ResponseEntity<Void> abortJob(
            @PathVariable UUID jobId,
            Authentication authentication
    ) {

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = UUID.fromString(principal.getUserId());

        jobService.abortJob(jobId, userId);
        return ResponseEntity.ok().build();
    }
}