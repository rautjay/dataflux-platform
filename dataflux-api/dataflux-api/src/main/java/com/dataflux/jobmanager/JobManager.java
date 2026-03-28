package com.dataflux.jobmanager;

import org.springframework.stereotype.Service;

import com.dataflux.config.FinalConfig;
import com.dataflux.model.Job;
import com.dataflux.repo.JobDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobManager {

    private final JobDao jobRepository;
    private final JobProcessor jobProcessor;

    public JobManager(JobDao jobRepository, JobProcessor jobProcessor) {
        this.jobRepository = jobRepository;
        this.jobProcessor = jobProcessor;
    }

    public void startProcessing(Job job, FinalConfig config) {

        log.info("Starting processing for jobId={}", job.getJobId());

        // 1. DUPLICATE CHECK (CRITICAL)
        if (!"CREATED".equals(job.getStatus())) {

            log.warn("Duplicate processing attempt for jobId={}, status={}",
                    job.getJobId(), job.getStatus());

            throw new IllegalStateException(
                    "Job already processed or in progress");
        }

        // 2. ATOMIC UPDATE (VERY IMPORTANT)
        int updated = jobRepository.updateStatusIfMatches(
                job.getJobId(),
                "CREATED",
                "PROCESSING");

        if (updated == 0) {

            log.warn("Concurrent request detected for jobId={}", job.getJobId());

            throw new IllegalStateException(
                    "Job already picked by another request");
        }

        log.info("Job locked for processing jobId={}", job.getJobId());

        // 🔥 3. TRIGGER ASYNC PROCESSING
        jobProcessor.processAsync(job, config);
    }

}
