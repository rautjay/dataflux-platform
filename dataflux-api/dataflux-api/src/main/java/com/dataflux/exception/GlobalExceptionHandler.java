package com.dataflux.exception;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileError(FileStorageException ex) {
        log.error("File error: {}", ex.getMessage());
        return ResponseEntity.status(500).body("File upload failed");
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<String> handleDBError(DatabaseException ex) {
        log.error("DB error: {}", ex.getMessage());
        return ResponseEntity.status(500).body("Database error");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500).body("Internal Server Error");
    }
}
