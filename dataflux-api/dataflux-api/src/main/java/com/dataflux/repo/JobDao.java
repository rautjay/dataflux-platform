package com.dataflux.repo;


import java.util.Optional;

import com.dataflux.model.Job;

public interface JobDao{

    void save(Job job);

    Optional<Job> getJobById(String jobId);
   
    void updateStatus(String jobId, String newStatus);
    
    int updateStatusIfMatches(String jobId, String expectedStatus, String newStatus);

    void incrementProcessedCount(String jobId, int count);

    void incrementFailedCount(String jobId, int count);
     void updateStartedAt(String jobId);

    void updateCompletedAt(String jobId);

    void updateTotalRecords(String jobId, int total);

    Optional<Job> findById(String jobId);

}
