package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.framework.config.FileConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class SysFileService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileConfig fileConfig;

    public SysFileService(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        if (file.getSize() > fileConfig.getMaxSize()) {
            throw new BusinessException("文件大小超过限制(" + (fileConfig.getMaxSize() / 1024 / 1024) + "MB)");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!isAllowType(ext)) {
            throw new BusinessException("不允许的文件类型:" + ext);
        }

        String path = fileConfig.getPath();
        String datePath = LocalDate.now().format(DATE_FMT);
        String dir = path + File.separator + "file" + File.separator + datePath;
        File targetDir = new File(dir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File targetFile = new File(targetDir, newFileName);

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败:" + e.getMessage());
        }

        return "/file/" + datePath + "/" + newFileName;
    }

    public File getFile(String filePath) {
        return new File(fileConfig.getPath(), filePath);
    }

    public boolean delete(String filePath) {
        File file = getFile(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private boolean isAllowType(String ext) {
        if (ext.isEmpty()) {
            return false;
        }
        List<String> allowList = Arrays.asList(fileConfig.getAllowTypes().split(","));
        return allowList.contains(ext);
    }
}
