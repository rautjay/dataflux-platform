package com.dataflux.config;

public class FinalConfig {

    private final boolean skipHeader;
    private final String delimiter;
    private final boolean failFast;
    private final String priority;

    public FinalConfig(
            boolean skipHeader,
            String delimiter,
            boolean failFast,
            String priority) {

        this.skipHeader = skipHeader;
        this.delimiter = delimiter;
        this.failFast = failFast;
        this.priority = priority;
    }

    public boolean isSkipHeader() {
        return skipHeader;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public String getPriority() {
        return priority;
    }
}
