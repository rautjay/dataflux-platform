package com.dataflux.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Job {

    private String jobId;
    private UUID tenantId;

    private String fileName;
    private String filePath;

    private String status;

    private int totalRecords;
    private int processedRecords;
    private int failedRecords;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String errorMessage;

    // 🔹 Constructors

    public Job() {
    }

    public Job(String jobId, UUID tenantId, String fileName, String filePath, String status, LocalDateTime createdAt) {
        this.jobId = jobId;
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.createdAt = createdAt;
    }

    // 🔹 Getters & Setters

    // public Job(String string, String string2, String string3, String string4, String string5,
    //         LocalDateTime localDateTime) {
    //     //TODO Auto-generated constructor stub
    // }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // 🔹 Utility Method

    public static Job createNew(String fileName, String filePath, UUID tenantId, String jobId) {
        Job job = new Job();
        job.setJobId(jobId);
        job.setFileName(fileName);
        job.setFilePath(filePath);
        job.setTenantId(tenantId);
        job.setStatus("CREATED");
        job.setCreatedAt(LocalDateTime.now());
        job.setTotalRecords(0);
        job.setProcessedRecords(0);
        job.setFailedRecords(0);
        return job;
    }
}