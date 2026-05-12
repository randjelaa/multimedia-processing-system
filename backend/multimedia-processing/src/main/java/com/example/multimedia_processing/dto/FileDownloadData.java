package com.example.multimedia_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class FileDownloadData {

    private Resource resource;
    private String contentType;
    private String fileName;
    private long contentLength;
}