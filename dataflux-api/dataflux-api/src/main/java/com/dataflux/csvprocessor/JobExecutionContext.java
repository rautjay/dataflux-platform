package com.dataflux.csvprocessor;
import java.util.concurrent.atomic.AtomicInteger;

public class JobExecutionContext {

    private final String jobId;
    private final AtomicInteger remainingBatches = new AtomicInteger(0);

    public JobExecutionContext(String jobId) {
        this.jobId = jobId;
    }

    public void incrementBatchCount() {
        remainingBatches.incrementAndGet();
    }

    public int decrementAndGet() {
        return remainingBatches.decrementAndGet();
    }

    public int getRemainingBatches() {
        return remainingBatches.get();
    }

    public String getJobId() {
        return jobId;
    }
}