package com.dataflux.dto;

import java.time.LocalDateTime;

public class FileUploadResponse {

  

    private String jobId;
    private String status;
    private String fileName;
    private LocalDateTime createdAt;
    private String message;

    // constructors, getters
    public FileUploadResponse() {
    }

    public FileUploadResponse(String jobId, String status, String fileName, LocalDateTime createdAt, String message) {
        this.jobId = jobId;
        this.status = status;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.message = message;
    }


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
}
