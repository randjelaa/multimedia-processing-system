package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.security.CustomUserPrincipal;
import com.example.multimedia_processing.service.JobService;
import com.example.multimedia_processing.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    private final JobService jobService;

    @PostMapping("/upload")
    public Job upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            Authentication authentication
    ) {

        CustomUserPrincipal principal =
                (CustomUserPrincipal)
                        authentication.getPrincipal();

        UUID userId = UUID.fromString(principal.getUserId());

        String objectKey = minioService.uploadFile(file);

        return jobService.createJob(
                file.getOriginalFilename(),
                objectKey,
                userId,
                type
        );
    }
}
