package com.dataflux.jobmanager;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dataflux.config.FinalConfig;
import com.dataflux.csvprocessor.CsvBatchProcessor;
import com.dataflux.model.Job;
import com.dataflux.repo.JobDao;
import com.dataflux.worker.TaskQueue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobProcessor {
    private final JobDao jobDao;
    private final CsvBatchProcessor csvBatchProcessor;
    private final TaskQueue taskQueue;

    public JobProcessor(JobDao jobDao, CsvBatchProcessor csvBatchProcessor, TaskQueue taskQueue) {
        this.jobDao = jobDao;
        this.csvBatchProcessor = csvBatchProcessor;
        this.taskQueue = taskQueue;
    }

    public void processAsync(Job job, FinalConfig config) {

        CompletableFuture
            .runAsync(
                () -> runJob(job, config),
                taskQueue.getExecutor()           // ← expose executor from TaskQueue
            )
            .exceptionally(ex -> {
                log.error("JOB FAILED | jobId={}", job.getJobId(), ex);
                jobDao.updateStatus(job.getJobId(), "FAILED");
                jobDao.updateCompletedAt(job.getJobId());
                return null;
            });

        }


      private void runJob(Job job, FinalConfig config) {
        log.info("JOB STARTED | jobId={} thread={}",
            job.getJobId(), Thread.currentThread().getName());
        long startTime = System.currentTimeMillis();
        jobDao.updateStartedAt(job.getJobId());
        csvBatchProcessor.process(job, config);
        jobDao.updateCompletedAt(job.getJobId());
        long duration = System.currentTimeMillis() - startTime;
        log.info("JOB COMPLETED | jobId={} | duration={} ms", job.getJobId(), duration);
    }
}