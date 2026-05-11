package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.entity.JobType;
import com.example.multimedia_processing.security.CustomUserPrincipal;
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

    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> downloadProcessedFile(@PathVariable UUID jobId) {
        Job job = jobService.getJobById(jobId);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        if (!JobStatus.DONE.equals(job.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        if (job.getResultFileKey() == null || job.getResultFileKey().isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }

        try {
            InputStream stream = minioService.getFile(job.getResultFileKey());
            StatObjectResponse metadata = minioService.getFileMetadata(job.getResultFileKey());

            String extension = switch (job.getType()) {
                case THUMBNAIL -> ".jpg";
                case AUDIO -> ".mp3";
                case TRANSCODE -> ".mp4";
                default -> "";
            };

            String baseName = job.getOriginalFileName().contains(".")
                    ? job.getOriginalFileName().substring(0, job.getOriginalFileName().lastIndexOf('.'))
                    : job.getOriginalFileName();
            String finalFileName = baseName + "_" + job.getType().toString().toLowerCase() + extension;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFileName + "\"")
                    .contentLength(metadata.size())
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
