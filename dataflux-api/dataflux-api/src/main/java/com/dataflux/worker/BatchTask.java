package com.dataflux.worker;

import com.dataflux.config.FinalConfig;
import com.dataflux.csvprocessor.JobExecutionContext;
import com.dataflux.model.Job;
import com.dataflux.repo.JobDao;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BatchTask implements Runnable {

    private final List<String[]> batch;
    private final Job job;
    private final FinalConfig config;
    private final BatchWorker batchWorker;
    private final JobDao jobDao;
    private final JobExecutionContext context;

    private static final int MAX_RETRIES = 3;

    public BatchTask(List<String[]> batch,
                     Job job,
                     FinalConfig config,
                     BatchWorker batchWorker,
                     JobDao jobDao, JobExecutionContext context) {
        this.batch = batch;
        this.job = job;
        this.config = config;
        this.batchWorker = batchWorker;
        this.jobDao = jobDao;
        this.context = context;
    }

    @Override
    public void run() {

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                batchWorker.processBatch(batch, job, config);

                log.info("Batch processed successfully | jobId={} batchSize={} attempt={}",
                        job.getJobId(), batch.size(), attempt);
                jobDao.incrementProcessedCount(job.getJobId(), batch.size());

                return;

            } catch (Exception ex) {
                attempt++;

                log.warn("Batch failed | jobId={} attempt={}", job.getJobId(), attempt, ex);

                if (attempt >= MAX_RETRIES) {

                    log.error("Batch permanently failed | jobId={}", job.getJobId());

                    jobDao.incrementFailedCount(job.getJobId(), batch.size());

                    batchWorker.handleBatchFailure(batch, job, ex);
                }
            }finally {
                int remaining = context.decrementAndGet();
                log.info("Batch completed | jobId={} remainingBatches={}", job.getJobId(), remaining);

                if (remaining == 0) {
                    log.info("All batches completed for jobId={}", job.getJobId());
                    jobDao.updateStatus(job.getJobId(), "COMPLETED");
                    jobDao.updateCompletedAt(job.getJobId());
                }
            }
        }
    }
}

