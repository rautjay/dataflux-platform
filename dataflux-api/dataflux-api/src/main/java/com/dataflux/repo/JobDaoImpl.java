package com.dataflux.repo;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.dataflux.model.Job;

@Repository
public class JobDaoImpl implements JobDao {

      private final JdbcTemplate jdbcTemplate;

    public JobDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Job> JOB_ROW_MAPPER = (rs, rowNum) -> {
        Job job = new Job();
        job.setJobId(rs.getString("job_id"));

        String tenantIdStr = rs.getString("tenant_id");
        if (tenantIdStr != null) {
            job.setTenantId(UUID.fromString(tenantIdStr));
        }

        job.setFileName(rs.getString("file_name"));
        job.setFilePath(rs.getString("file_path"));
        job.setStatus(rs.getString("status"));
        job.setTotalRecords(rs.getInt("total_records"));
        job.setProcessedRecords(rs.getInt("processed_records"));
        job.setFailedRecords(rs.getInt("failed_records"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) job.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) job.setStartedAt(startedAt.toLocalDateTime());

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) job.setCompletedAt(completedAt.toLocalDateTime());

        job.setErrorMessage(rs.getString("error_message"));
        return job;
    };


    @Override
    public void save(Job job) {
       
         jdbcTemplate.update(
            "INSERT INTO jobs (job_id, tenant_id, file_name, file_path, status, created_at) VALUES (?, ?, ?, ?, ?, ?)",
            job.getJobId(),
            job.getTenantId(),
            job.getFileName(),
            job.getFilePath(),
            job.getStatus(),
            job.getCreatedAt()
        );
}

      @Override
    public Optional<Job> getJobById(String jobId) {
        List<Job> results = jdbcTemplate.query(
            """
            SELECT job_id, tenant_id, file_name, file_path, status,
                   total_records, processed_records, failed_records,
                   created_at, started_at, completed_at, error_message
            FROM jobs
            WHERE job_id = ?
            """,
            JOB_ROW_MAPPER,
            jobId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public int updateStatusIfMatches(String jobId, String expectedStatus, String newStatus) {
        return jdbcTemplate.update(
            "UPDATE jobs SET status = ? WHERE job_id = ? AND status = ?",
            newStatus,
            jobId,
            expectedStatus
        );
    }

    @Override
    public void updateStatus(String jobId, String newStatus) {
        jdbcTemplate.update(
            "UPDATE jobs SET status = ? WHERE job_id = ?",
            newStatus,
            jobId
        );
    }

    @Override
    public void incrementProcessedCount(String jobId, int count) {
        jdbcTemplate.update(
            "UPDATE jobs SET processed_records = processed_records + ? WHERE job_id = ?",
            count,
            jobId
        );
    }

    @Override
    public void incrementFailedCount(String jobId, int count) {
        jdbcTemplate.update(
            "UPDATE jobs SET failed_records = failed_records + ? WHERE job_id = ?",
            count,
            jobId
        );
    }

     @Override
    public void updateStartedAt(String jobId) {
        jdbcTemplate.update(
            "UPDATE jobs SET started_at = NOW() WHERE job_id = ?",
            jobId
        );
    }

    @Override
    public void updateCompletedAt(String jobId) {
        jdbcTemplate.update(
            "UPDATE jobs SET completed_at = NOW() WHERE job_id = ?",
            jobId
        );
    }

    @Override
    public void updateTotalRecords(String jobId, int total) {
        jdbcTemplate.update(
            "UPDATE jobs SET total_records = ? WHERE job_id = ?",
            total, jobId
        );
    }

    // ── Read Operations ──────────────────────────────────────────────────────────

    @Override
    public Optional<Job> findById(String jobId) {
        List<Job> results = jdbcTemplate.query(
            """
            SELECT job_id, tenant_id, file_name, file_path, status,
                   total_records, processed_records, failed_records,
                   created_at, started_at, completed_at, error_message
            FROM jobs
            WHERE job_id = ?
            """,
            JOB_ROW_MAPPER,
            jobId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
