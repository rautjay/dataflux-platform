package com.dataflux.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FileProcessRequest {

    private String fileName;
    private String jobId;
    private Boolean skipHeader;
    private String delimiter;
    private Boolean failFast;
    private String priority;



}
