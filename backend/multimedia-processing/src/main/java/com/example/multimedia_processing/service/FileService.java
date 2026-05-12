package com.example.multimedia_processing.service;

import com.example.multimedia_processing.dto.FileDownloadData;
import com.example.multimedia_processing.entity.Job;
import com.example.multimedia_processing.entity.JobStatus;
import com.example.multimedia_processing.repository.JobRepository;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final JobRepository jobRepository;
    private final MinioService minioService;

    public FileDownloadData downloadProcessedFile(
            UUID jobId,
            UUID userId
    ) {

        Job job = jobRepository.findByIdAndUser_Id(jobId, userId).orElseThrow(() -> new RuntimeException("Job not found"));

        if (!JobStatus.DONE.equals(job.getStatus())) {
            throw new RuntimeException("Job is not finished");
        }

        if (job.getResultFileKey() == null || job.getResultFileKey().isEmpty()) {
            throw new RuntimeException("Result file missing");
        }

        try {

            InputStream stream = minioService.getFile(job.getResultFileKey());
            StatObjectResponse metadata = minioService.getFileMetadata(job.getResultFileKey());

            String extension = switch (job.getType()) {
                case THUMBNAIL -> ".jpg";
                case AUDIO -> ".mp3";
                case TRANSCODE -> ".mp4";
            };

            String baseName = job.getOriginalFileName().contains(".") ?
                              job.getOriginalFileName().substring(0, job.getOriginalFileName().lastIndexOf('.')) :
                              job.getOriginalFileName();

            String finalFileName = baseName + "_" + job.getType().toString().toLowerCase() + extension;

            return new FileDownloadData(
                    new InputStreamResource(stream),
                    metadata.contentType(),
                    finalFileName,
                    metadata.size()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }
}