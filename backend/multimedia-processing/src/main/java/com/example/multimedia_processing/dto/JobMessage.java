package com.example.multimedia_processing.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JobMessage {

    private String jobId;
    private String objectKey;
    private String jobType;
}
