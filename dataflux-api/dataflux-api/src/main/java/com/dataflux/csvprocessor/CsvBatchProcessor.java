package com.dataflux.csvprocessor;


import com.dataflux.config.FinalConfig;
import com.dataflux.model.Job;
import com.dataflux.repo.JobDao;
import com.dataflux.worker.BatchTask;
import com.dataflux.worker.BatchWorker;
import com.dataflux.worker.TaskQueue;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CsvBatchProcessor {

    @Value("${dataflux.csv.batchsize:1000}")
    private int batchsize;

    private final BatchWorker batchWorker;
    private final JobDao jobDao;
    private final TaskQueue taskQueue;

    public CsvBatchProcessor(BatchWorker batchWorker,
                             JobDao jobDao,
                             TaskQueue taskQueue) {
        this.batchWorker = batchWorker;
        this.jobDao = jobDao;
        this.taskQueue = taskQueue;
    }

    public void process(Job job, FinalConfig config) {

        log.info("CSV processing started | jobId={} priority={}",
                job.getJobId(), config.getPriority());

        Path filePath = Path.of(job.getFilePath());

        int  batchSize = batchsize;
        boolean skipHeader = config.isSkipHeader();
        //change config delimeter from string to char
        char delimiter = config.getDelimiter().charAt(0);
        boolean failFast = config.isFailFast();

        List<String[]> batch = new ArrayList<>(batchSize);

      JobExecutionContext context = new JobExecutionContext(job.getJobId());

        int totalRead = 0;

        try (
            CSVReader reader = new CSVReaderBuilder(
                    new BufferedReader(Files.newBufferedReader(filePath)))
                    .withCSVParser(
                            new CSVParserBuilder()
                                    .withSeparator(delimiter) // ✅ delimiter applied
                                    .build()
                    )
                    .build()
        ) {

            String[] line;

            // Header handling
            if (skipHeader) {
                reader.readNext();
                log.info("Header skipped | jobId={}", job.getJobId());
            }

            while ((line = reader.readNext()) != null) {

                totalRead++;
                batch.add(line);

                if (batch.size() == batchSize) {
                    submitBatch(new ArrayList<>(batch), job, config,context);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                submitBatch(new ArrayList<>(batch), job, config,context);
            }

            log.info("All batches submitted | jobId={} totalRecords={}",
                    job.getJobId(), totalRead);

        } catch (Exception e) {

            log.error("CSV reading failed | jobId={}", job.getJobId(), e);

            jobDao.updateStatus(job.getJobId(), "FAILED");

            throw new RuntimeException(e);
        }
    }

    private void submitBatch(List<String[]> batch,
                             Job job,
                             FinalConfig config,
                            JobExecutionContext context) {

        context.incrementBatchCount();

        BatchTask task = new BatchTask(
                batch,
                job,
                config,
                batchWorker,
                jobDao,
                context
        );

        taskQueue.addTask(task);
    }
}