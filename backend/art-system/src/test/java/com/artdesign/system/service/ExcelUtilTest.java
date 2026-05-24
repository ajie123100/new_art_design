package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.common.utils.ExcelUtil;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelUtilTest {

    @Test
    void readExcelRejectsNonXlsxFile() {
        Part part = new SimplePart("data.csv", "a,b".getBytes());

        assertThatThrownBy(() -> ExcelUtil.readExcel(part, Row.class))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅支持导入xlsx文件");
    }

    @Test
    void readExcelRejectsEmptyFile() {
        Part part = new SimplePart("empty.xlsx", new byte[0]);

        assertThatThrownBy(() -> ExcelUtil.readExcel(part, Row.class))
                .isInstanceOf(BusinessException.class)
                .hasMessage("导入文件不能为空");
    }

    static class Row {
    }

    private record SimplePart(String fileName, byte[] data) implements Part {
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getSubmittedFileName() {
            return fileName;
        }

        @Override
        public long getSize() {
            return data.length;
        }

        @Override
        public void write(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public java.util.Collection<String> getHeaders(String name) {
            return java.util.List.of();
        }

        @Override
        public java.util.Collection<String> getHeaderNames() {
            return java.util.List.of();
        }
    }
}
