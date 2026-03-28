package com.dataflux.util;

import java.io.File;
import java.io.FileOutputStream;

import com.dataflux.model.Job;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobUtils {

    public static String generateJobId() {
        return "job-" + System.currentTimeMillis()+"-" + (int)(Math.random() * 1000);
    }

    public static String generateFilePath(String uploadDir, String jobId, String fileName) {
        return uploadDir + "/" + jobId;
    }

    public static String generateTempFilePath(String tmpDir, String jobId) {
        return tmpDir + "/" + jobId;
    }

    public static boolean isValidFileType(String fileName) {
        return fileName != null && fileName.endsWith(".csv");
    }

    public static boolean isValidFileSize(byte[] fileBytes, long maxSize) {
        return fileBytes != null && fileBytes.length <= maxSize;
    }

    public static String getTenantIdFromContext() {
       return "tenant-" + System.currentTimeMillis()+"-" + (int)(Math.random() * 1000);
    }

    public static void saveFile(String uploadDir, String jobId,byte[] fileBytes, String fileName){
       
        try {
            // Create directory for seperate jobs
            log.info("Creating directory for Job ID: {} at path: {}", jobId, uploadDir + "/" + jobId);
            File dir = new File(uploadDir + "/" + jobId);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 5. Save file
            File file = new File(uploadDir + "/" + jobId + "/" + fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                log.info("Saving file for Job ID: {} at path: {}", jobId, file.getAbsolutePath());
                fos.write(fileBytes);
            }

        } catch (Exception e) {
            log.error("Failed to upload file for Job ID: {}", jobId, e.getMessage());
            throw new RuntimeException("File upload failed", e);
        }
    }


public static Boolean validateFilename(String filename, Job job) {
    // check if filename present in a request is same as filename stored in job details.
    log.info("Validating filename: {}", filename);
    if(filename == null || job == null || job.getFileName() == null) {
        return false;
    }
    log.info("Comparing filename: {} with job filename: {}", filename, job.getFileName());
    return filename.equals(job.getFileName());

}
}