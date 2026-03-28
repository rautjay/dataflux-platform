package com.dataflux.service;

import com.dataflux.config.DefaultConfig;
import com.dataflux.config.FinalConfig;
import com.dataflux.dto.FileProcessRequest;
import com.dataflux.dto.FileUploadRequest;
import com.dataflux.dto.FileUploadResponse;
import com.dataflux.jobmanager.JobManager;
import com.dataflux.model.Job;
import com.dataflux.repo.JobDao;
import com.dataflux.util.JobUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class JobService {

    private final JobDao jobRepository;
    private final DefaultConfig defaultConfig;
    private final JobManager jobManager;

    @Value("${dataflux.file.upload-dir}")
    private String uploadDir;

    @Value("${dataflux.file.max-size}")
    private long MAX_SIZE;

    @Value("${dataflux.file.tmp-dir}")
    private String tmpDir;

    public JobService(JobDao jobRepository, DefaultConfig defaultConfig, JobManager jobManager) {
        this.jobRepository = jobRepository;
        this.defaultConfig = defaultConfig;
        this.jobManager = jobManager;
    }

    public FileUploadResponse createJob(FileUploadRequest request) {
        log.info("Received file upload request: {}", request.getFileName());
        if (!request.getFileName().endsWith(".csv")) {
            log.warn("Invalid file type: {}", request.getFileName());

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "Invalid file type. Only CSV allowed");
        }

        if (request.getFileName() == null || request.getFileContent() == null) {
            log.warn("Missing file name or content");

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "File upload failed");

        }
        byte[] fileBytes = Base64.getDecoder().decode(request.getFileContent());
        if (fileBytes.length > MAX_SIZE) {
            log.warn("File too large: {}", request.getFileName());

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "File upload failed");
        }

        // 1. Generate Job ID
        String jobId = JobUtils.generateJobId();
        log.info("Generated Job ID: {} for file: {}", jobId, request.getFileName());
        // 2. Build file path
        // String tmpFilePath = JobUtils.generateTempFilePath(tmpDir, jobId);
        log.info("Generated temporary file path: {} for Job ID: {}", tmpDir, jobId);

        // 3. Save file to temp location
        try {
            JobUtils.saveFile(tmpDir, jobId, fileBytes, request.getFileName());
            log.info("File saved successfully for Job ID: {} at path: {}", jobId, tmpDir + "/" + jobId);
        } catch (Exception e) {
            log.error("Failed to save file for Job ID: {} at path: {}. Error: {}", jobId, tmpDir + "/" + jobId,
                    e.getMessage());
            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "File upload failed");
        }

        String filePath = JobUtils.generateFilePath(uploadDir, jobId, request.getFileName());

        // 6. Create Job object
        Job job = Job.createNew(request.getFileName(), filePath, UUID.randomUUID(), jobId);

        // TODO: tenantId from JWT later

        // 7. Save to DB
        log.info("Saving job to database for Job ID: {}", jobId);
        try {
            jobRepository.save(job);
        } catch (Exception e) {
            log.error("Failed to save job to database for Job ID: {}", jobId, e);
            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "File upload failed");
        }

        try {
            // Move file from temp to final location
            File tempFile = new File(tmpDir + "/" + jobId);
            File finalFile = new File(filePath + "/");
            if (tempFile.renameTo(finalFile)) {
                log.info("File moved successfully from tmp to uploads for Job ID: {} from {} to {}", jobId,
                        tmpDir + "/" + jobId, filePath);
            } else {
                log.error("Failed to move file from tmp to uploads for Job ID: {} from {} to {}", jobId,
                        tmpDir + "/" + jobId, filePath);
                throw new RuntimeException("File move failed");
            }
        } catch (Exception e) {
            log.error("Error while moving file for Job ID: {}. Error: {}", jobId, e.getMessage());
            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    request.getFileName(),
                    LocalDateTime.now(),
                    "File upload failed");
        }

        // 8. Response
        return new FileUploadResponse(
                jobId,
                "CREATED",
                request.getFileName(),
                job.getCreatedAt(),
                "File uploaded successfully");
    }

    public FileUploadResponse createJobFromMultipart(MultipartFile file) {

        if (file == null) {
            log.warn("No file received in request");

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    null,
                    LocalDateTime.now(),
                    "File is missing");
        }

        // ✅ 1. Resolve file name (request override OR original)
        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            log.warn("File name is invalid");

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    null,
                    LocalDateTime.now(),
                    "Invalid file name");
        }

        // ✅ 2. Sanitize file name (VERY IMPORTANT - security)
        fileName = Paths.get(fileName).getFileName().toString();

        log.info("Processing file: {}", fileName);

        // ✅ 3. Validate file type
        if (!fileName.toLowerCase().endsWith(".csv")) {
            log.warn("Invalid file type: {}", fileName);

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "Invalid file type. Only CSV allowed");
        }

        // ✅ 4. Validate empty file
        if (file.isEmpty()) {
            log.warn("Empty file received: {}", fileName);

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "File is empty");
        }

        // ✅ 5. Validate size
        if (file.getSize() > MAX_SIZE) {
            log.warn("File too large: {} size: {}", fileName, file.getSize());

            return new FileUploadResponse(
                    null,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "File size exceeds allowed limit");
        }

        // ✅ 6. Generate Job ID
        String jobId = JobUtils.generateJobId();
        log.info("Generated Job ID: {} for file: {}", jobId, fileName);

        // ✅ 7. Prepare temp file path
        Path tempDirPath = Paths.get(tmpDir, jobId);
        Path tempFilePath = tempDirPath.resolve(fileName);

        // Ensure temp directory exists
        try {
            Files.createDirectories(tempDirPath);
            file.transferTo(tempFilePath.toFile());
            log.info("File saved to temp location: {}", tempFilePath);
        } catch (Exception e) {
            log.error("Failed to create temp directory: {}", tmpDir, e);

            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "Internal error (temp dir)");
        }

        // // ✅ 8. Save file to temp (STREAMING - NO MEMORY ISSUE)
        // try {
        // file.transferTo(tempPath.toFile());
        // log.info("File saved to temp location: {}", tempPath);
        // } catch (Exception e) {
        // log.error("Failed to save file to temp for Job ID: {}", jobId, e);

        // return new FileUploadResponse(
        // jobId,
        // "FAILED",
        // fileName,
        // LocalDateTime.now(),
        // "File upload failed"
        // );
        // }

        // ✅ 9. Generate final file path
        Path finalDirPath = Paths.get(uploadDir, jobId);
        Path finalFilePath = finalDirPath.resolve(fileName);

        try {
            Files.createDirectories(finalDirPath);
        } catch (Exception e) {
            log.error("Failed to create upload directory: {}", finalDirPath, e);

            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "Internal error (upload dir)");
        }

        // ✅ 10. Create Job entity
        Job job = Job.createNew(
                fileName,
                finalFilePath.toString(),
                UUID.randomUUID(),
                jobId);

        // ✅ 11. Save to DB
        try {
            log.info("Saving job to database for Job ID: {}", jobId);
            jobRepository.save(job);
        } catch (Exception e) {
            log.error("Failed to save job to database for Job ID: {}", jobId, e);

            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "Database error");
        }

        // ✅ 12. Move file from temp → final location
        try {
            Files.move(
                    tempFilePath,
                    finalFilePath,
                    StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(tempDirPath);
            log.info("File moved successfully from {} to {}", tempFilePath, finalFilePath);

        } catch (Exception e) {
            log.error("Failed to move file for Job ID: {}", jobId, e);

            return new FileUploadResponse(
                    jobId,
                    "FAILED",
                    fileName,
                    LocalDateTime.now(),
                    "File move failed");
        }

        // ✅ 13. Success response
        return new FileUploadResponse(
                jobId,
                "CREATED",
                fileName,
                job.getCreatedAt(),
                "File uploaded successfully");
    }

    public void processJob(FileProcessRequest request) {

        log.info(" Received request to process file: {} for Job ID: {}", request.getFileName(), request.getJobId());

        if (request.getFileName() == null || request.getJobId() == null) {
            log.warn("Missing file name or job ID in process request");
            throw new IllegalArgumentException("File name and Job ID are required for processing");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found for ID: " + request.getJobId()));
        if (job == null) {
            log.warn("No job found for Job ID: {}", request.getJobId());
            throw new IllegalArgumentException("Invalid Job ID: " + request.getJobId());
        }
        // validate filename using jobId.
        if (!JobUtils.validateFilename(request.getFileName(), job)) {
            log.warn("Filename in request: {} does not match job filename: {} for Job ID: {}", request.getFileName(),
                    job.getFileName(), request.getJobId());
            throw new IllegalArgumentException("Filename does not match job details");
        }

        // validate file existence in storage before processing.
        Path filePath = Paths.get(job.getFilePath());

        if (!Files.exists(filePath)) {
            log.warn("File not found at path: {} for Job ID: {}", filePath, request.getJobId());
            throw new IllegalArgumentException("File not found for processing");
        }
        // getting final config by resolving default config and request override.`
        log.info("Resolving processing options for Job ID: {}", request.getJobId());
        FinalConfig config = resolveProcessingOptions(request);
        log.info("Final processing config for Job ID: {} is skipHeader: {}, delimiter: {}, failFast: {}, priority: {}",
                request.getJobId(), config.isSkipHeader(), config.getDelimiter(), config.isFailFast(),
                config.getPriority());
        // 5. Hand off to JobManager — handles concurrency lock + async dispatch
        log.info("Dispatching job for processing | jobId={}", request.getJobId());
        jobManager.startProcessing(job, config);

    }

    private FinalConfig resolveProcessingOptions(FileProcessRequest request) {

        return new FinalConfig(
                request.getSkipHeader() != null
                        ? request.getSkipHeader()
                        : defaultConfig.isSkipHeader(),

                request.getDelimiter() != null && !request.getDelimiter().isEmpty()
                        ? request.getDelimiter()
                        : defaultConfig.getDelimiter(),

                request.getFailFast() != null
                        ? request.getFailFast()
                        : defaultConfig.isFailFast(),

                request.getPriority() != null && !request.getPriority().isEmpty()
                        ? request.getPriority()
                        : defaultConfig.getPriority());
    }
}