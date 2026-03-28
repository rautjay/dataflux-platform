package com.dataflux.controller;

import com.dataflux.dto.FileProcessRequest;
import com.dataflux.dto.FileUploadRequest;
import com.dataflux.dto.FileUploadResponse;
import com.dataflux.model.Job;
import com.dataflux.service.JobService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@Slf4j
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestBody FileUploadRequest request) {
        log.info("Received file upload request to dataflux: {}", request.getFileName());
        FileUploadResponse response = jobService.createJob(request);

        return ResponseEntity.ok(response);
    }

     
    @PostMapping("/upload-multipart")
    public ResponseEntity<FileUploadResponse> uploadMultiPartFile( @RequestParam("file") MultipartFile file){
        log.info("Received multipart file upload request to dataflux: {}", file.getOriginalFilename());
        FileUploadResponse response = jobService.createJobFromMultipart(file);

        return ResponseEntity.ok(response);
        
    }



    @PostMapping("/process")
    public ResponseEntity<FileUploadResponse> processFile(@RequestBody FileProcessRequest request) {
        log.info("Received file processing request for file: {}", request.getFileName());
        jobService.processJob(request);
        return ResponseEntity.ok(
                new FileUploadResponse(
                        request.getJobId(),
                        "Processing Started",
                        request.getFileName(),
                        LocalDateTime.now(),
                        "File Processing Started Successfully"
                )
        );
    }
}