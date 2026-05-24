package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.core.domain.R;
import com.artdesign.system.service.SysFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/file")
public class SysFileController {
    private final SysFileService fileService;

    public SysFileController(SysFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @SaCheckPermission("system:file:upload")
    public R<UploadResult> upload(@RequestParam MultipartFile file) {
        String url = fileService.upload(file);
        return R.ok(new UploadResult(url));
    }

    @GetMapping("/download")
    @SaCheckPermission("system:file:download")
    public void download(@RequestParam String url, HttpServletResponse response) throws IOException {
        File file = fileService.getFile(url);
        String filename = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLengthLong(file.length());
        try (OutputStream outputStream = response.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
        }
    }

    @DeleteMapping
    @SaCheckPermission("system:file:delete")
    public R<Void> delete(@RequestParam String url) {
        fileService.delete(url);
        return R.ok();
    }

    public record UploadResult(String url) {
    }
}
