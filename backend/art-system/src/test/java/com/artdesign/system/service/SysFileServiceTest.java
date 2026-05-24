package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.framework.config.FileConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SysFileServiceTest {
    @TempDir
    Path tempDir;

    private SysFileService fileService;

    @BeforeEach
    void setUp() {
        FileConfig config = new FileConfig();
        config.setPath(tempDir.toString());
        config.setAllowTypes("png,txt,xlsx");
        config.setAllowContentTypes("image/,text/plain,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileService = new SysFileService(config);
    }

    @Test
    void uploadRejectsImageWithMismatchedContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not really a png".getBytes()
        );

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("图片文件内容与扩展名不匹配");
    }

    @Test
    void uploadAcceptsTextFileAndPreventsTraversalOnRead() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "hello".getBytes()
        );

        String url = fileService.upload(file);

        assertThat(url).startsWith("/file/");
        assertThat(fileService.getFile(url)).isFile();
        assertThatThrownBy(() -> fileService.getFile("../application.yml"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件路径非法");
    }

    @Test
    void uploadAcceptsXlsxContentTypeAndZipMagic() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0x50, 0x4B, 0x03, 0x04}
        );

        String url = fileService.upload(file);

        assertThat(url).startsWith("/file/");
        assertThat(fileService.getFile(url)).isFile();
    }
}
