package com.artdesign.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {

    private String path = "uploads";
    private long maxSize = 10 * 1024 * 1024;
    private String allowTypes = "jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,txt,zip,rar";
    private String allowContentTypes = "image/,application/pdf,text/plain,application/msword,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/zip,application/x-zip-compressed,application/vnd.rar,application/x-rar-compressed";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public String getAllowTypes() {
        return allowTypes;
    }

    public void setAllowTypes(String allowTypes) {
        this.allowTypes = allowTypes;
    }

    public String getAllowContentTypes() {
        return allowContentTypes;
    }

    public void setAllowContentTypes(String allowContentTypes) {
        this.allowContentTypes = allowContentTypes;
    }
}
