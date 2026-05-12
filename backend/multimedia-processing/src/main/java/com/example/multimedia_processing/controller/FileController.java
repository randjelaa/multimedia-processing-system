package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.dto.FileDownloadData;
import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.security.CustomUserPrincipal;
import com.example.multimedia_processing.service.FileService;
import com.example.multimedia_processing.service.JobService;
import com.example.multimedia_processing.service.MinioService;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;
    private final FileService fileService;
    private final JobService jobService;

    @PostMapping("/upload")
    public Job upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            Authentication authentication
    ) {

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = UUID.fromString(principal.getUserId());

        String objectKey = minioService.uploadFile(file);

        return jobService.createJob(
                file.getOriginalFilename(),
                objectKey,
                userId,
                type
        );
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> downloadProcessedFile(
            @PathVariable UUID jobId,
            Authentication authentication
    ) {

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = UUID.fromString(principal.getUserId());

        FileDownloadData file = fileService.downloadProcessedFile(jobId, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                file.getFileName() +
                                "\""
                )
                .contentLength(file.getContentLength())
                .body(file.getResource());
    }
}
