package com.example.multimedia_processing.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JobResultMessage {

    private String jobId;
    private String status;
    private String resultFileKey;
    private Integer progressPercentage;
}