package com.artdesign.system.domain.dto;

public record DatabaseInfo(
        String databaseProductName,
        String databaseProductVersion,
        String driverName,
        String driverVersion,
        String url,
        String userName,
        int maxConnections,
        int activeConnections,
        int idleConnections
) {
}
