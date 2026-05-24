package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.DatabaseInfo;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Service
public class SysDatabaseService {
    private final DataSource dataSource;

    public SysDatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DatabaseInfo getInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            HikariDataSource hikari = dataSource instanceof HikariDataSource ds ? ds : null;
            return new DatabaseInfo(
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getDriverName(),
                    metaData.getDriverVersion(),
                    maskUrl(metaData.getURL()),
                    metaData.getUserName(),
                    hikari == null ? 0 : hikari.getMaximumPoolSize(),
                    hikari == null || hikari.getHikariPoolMXBean() == null ? 0 : hikari.getHikariPoolMXBean().getActiveConnections(),
                    hikari == null || hikari.getHikariPoolMXBean() == null ? 0 : hikari.getHikariPoolMXBean().getIdleConnections()
            );
        } catch (Exception ex) {
            throw new BusinessException("数据库监控信息获取失败");
        }
    }

    private String maskUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceAll("(?i)(password=)[^&;]+", "$1******");
    }
}
