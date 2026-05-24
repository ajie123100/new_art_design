package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.framework.config.FileConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
        validateContentType(file);
        validateMagic(file, ext);

        String datePath = LocalDate.now().format(DATE_FMT);
        File uploadRoot = new File(fileConfig.getPath()).getAbsoluteFile();
        String dir = uploadRoot.getPath() + File.separator + "file" + File.separator + datePath;
        File targetDir = new File(dir);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new BusinessException("上传目录创建失败，请联系管理员");
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File targetFile = new File(targetDir, newFileName).getAbsoluteFile();

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败，请稍后重试");
        }

        return "/file/" + datePath + "/" + newFileName;
    }

    public File getFile(String filePath) {
        Path root = Paths.get(fileConfig.getPath()).toAbsolutePath().normalize();
        String relativePath = normalizeRelativePath(filePath);
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件路径非法");
        }
        File file = target.toFile();
        if (!file.isFile()) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    public boolean delete(String filePath) {
        File file = getFile(filePath);
        if (!file.delete()) {
            throw new BusinessException("文件删除失败");
        }
        return true;
    }

    private String getExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
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

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        boolean allowed = Arrays.stream(fileConfig.getAllowContentTypes().split(","))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .anyMatch(type -> normalized.equals(type) || (type.endsWith("/") && normalized.startsWith(type)));
        if (!allowed) {
            throw new BusinessException("不允许的文件内容类型:" + contentType);
        }
    }

    private void validateMagic(MultipartFile file, String ext) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(8);
            if (header.length == 0) {
                throw new BusinessException("上传文件不能为空");
            }
            if (isImage(ext) && !hasImageMagic(header, ext)) {
                throw new BusinessException("图片文件内容与扩展名不匹配");
            }
            if ("pdf".equals(ext) && !startsWith(header, "%PDF".getBytes())) {
                throw new BusinessException("PDF文件内容与扩展名不匹配");
            }
            if (isZipFamily(ext) && !startsWith(header, new byte[]{0x50, 0x4B})) {
                throw new BusinessException("压缩或Office文件内容与扩展名不匹配");
            }
            if ("rar".equals(ext) && !startsWith(header, new byte[]{0x52, 0x61, 0x72, 0x21})) {
                throw new BusinessException("RAR文件内容与扩展名不匹配");
            }
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请稍后重试");
        }
    }

    private boolean isImage(String ext) {
        return List.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext);
    }

    private boolean isZipFamily(String ext) {
        return List.of("zip", "docx", "xlsx").contains(ext);
    }

    private boolean hasImageMagic(byte[] header, String ext) {
        return switch (ext) {
            case "jpg", "jpeg" -> startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case "png" -> startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
            case "gif" -> startsWith(header, "GIF".getBytes());
            case "bmp" -> startsWith(header, "BM".getBytes());
            case "webp" -> startsWith(header, "RIFF".getBytes());
            default -> false;
        };
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String normalizeRelativePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new BusinessException("文件路径不能为空");
        }
        String path = filePath.trim().replace("\\", "/");
        if (path.startsWith("/file/")) {
            return path.substring(1);
        }
        if (path.startsWith("file/")) {
            return path;
        }
        throw new BusinessException("文件路径非法");
    }
}
