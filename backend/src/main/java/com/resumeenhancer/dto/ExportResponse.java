package com.resumeenhancer.dto;

public class ExportResponse {
    private String downloadUrl;
    private String format;
    private String filename;
    private Long fileSize;

    public ExportResponse() {}

    public ExportResponse(String downloadUrl, String format, String filename, Long fileSize) {
        this.downloadUrl = downloadUrl;
        this.format = format;
        this.filename = filename;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
