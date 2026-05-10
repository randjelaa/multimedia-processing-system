package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;

    @GetMapping
    public List<Job> getJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/{id}")
    public Job getJob(
            @PathVariable UUID id
    ) {

        return jobRepository.findById(id)
                .orElseThrow();
    }
}