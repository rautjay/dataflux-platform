
package com.dataflux.worker;

import com.dataflux.config.FinalConfig;
import com.dataflux.model.Job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BatchWorker {

    public void processBatch(List<String[]> batch,
                             Job job,
                             FinalConfig config) {

        for (String[] record : batch) {

            try {
                // 🔥 Rule Engine (next step)
                // we will fetch rules from redis and apply on records.


                // 🔥 Save processed record
                // processedRepo.save(...);

            } catch (Exception e) {
                handleRecordError(record, job, e);
            }
        }
    }

    public void handleBatchFailure(List<String[]> batch, Job job, Exception ex) {

        for (String[] record : batch) {
            handleRecordError(record, job, ex);
        }
    }

    private void handleRecordError(String[] record, Job job, Exception e) {

        // Save into error_records
        log.error("Record failed | jobId={} data={}", job.getJobId(), record, e);
    }
}
