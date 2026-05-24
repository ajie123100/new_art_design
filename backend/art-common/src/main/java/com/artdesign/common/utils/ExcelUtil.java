package com.artdesign.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.artdesign.common.exception.BusinessException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtil {

    public static void writeExcel(HttpServletResponse response, List<?> data, Class<?> head, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + encoded + ".xlsx");
        EasyExcel.write(response.getOutputStream(), head)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(fileName)
                .doWrite(data);
    }

    public static <T> List<T> readExcel(Part part, Class<T> head) throws IOException {
        if (part == null || part.getSize() == 0) {
            throw new BusinessException("导入文件不能为空");
        }
        String fileName = part.getSubmittedFileName();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("仅支持导入xlsx文件");
        }
        try {
            List<T> rows = EasyExcel.read(part.getInputStream(), head, null).sheet().doReadSync();
            if (rows == null || rows.isEmpty()) {
                throw new BusinessException("导入文件没有可读取的数据行");
            }
            return rows;
        } catch (BusinessException ex) {
            throw ex;
        } catch (ExcelAnalysisException ex) {
            throw new BusinessException("导入文件解析失败，请检查模板格式");
        }
    }
}
