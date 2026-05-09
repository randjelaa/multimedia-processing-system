package com.example.multimedia_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalFileName;

    private String objectKey;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    private String resultFileKey;

    private JobType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
