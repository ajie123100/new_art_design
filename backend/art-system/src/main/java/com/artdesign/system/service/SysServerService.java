package com.artdesign.system.service;

import com.artdesign.system.domain.dto.ServerInfo;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SysServerService {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public ServerInfo getInfo() {
        return new ServerInfo(
                getCpu(),
                getJvmMemory(),
                getJvm(),
                getSys(),
                getFiles()
        );
    }

    private ServerInfo.Cpu getCpu() {
        int count = Runtime.getRuntime().availableProcessors();
        return new ServerInfo.Cpu(0, count);
    }

    private ServerInfo.Memory getJvmMemory() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = mem.getHeapMemoryUsage();
        long total = usage.getCommitted() / 1024 / 1024;
        long used = usage.getUsed() / 1024 / 1024;
        long free = total - used;
        double percent = total > 0 ? used * 100.0 / total : 0;
        return new ServerInfo.Memory(total, used, free, Double.parseDouble(DF.format(percent)));
    }

    private ServerInfo.Jvm getJvm() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = mem.getHeapMemoryUsage();
        long total = usage.getCommitted() / 1024 / 1024;
        long used = usage.getUsed() / 1024 / 1024;
        long free = total - used;
        long start = ManagementFactory.getRuntimeMXBean().getStartTime();
        long run = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        String time = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(start), ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new ServerInfo.Jvm(
                System.getProperty("java.vm.name"),
                Runtime.version().toString(),
                total, used, free,
                Double.parseDouble(DF.format(total > 0 ? used * 100.0 / total : 0)),
                System.getProperty("user.dir"),
                time,
                run
        );
    }

    private ServerInfo.Sys getSys() {
        try {
            return new ServerInfo.Sys(
                    java.net.InetAddress.getLocalHost().getHostName(),
                    System.getProperty("os.name"),
                    System.getProperty("user.dir")
            );
        } catch (Exception e) {
            return new ServerInfo.Sys("unknown", System.getProperty("os.name"), System.getProperty("user.dir"));
        }
    }

    private List<ServerInfo.SysFile> getFiles() {
        File[] roots = File.listRoots();
        List<ServerInfo.SysFile> list = new ArrayList<>();
        for (File f : roots) {
            long total = f.getTotalSpace() / 1024 / 1024 / 1024;
            long free = f.getFreeSpace() / 1024 / 1024 / 1024;
            list.add(new ServerInfo.SysFile(
                    f.getPath(),
                    total, total - free, free,
                    Double.parseDouble(DF.format(total > 0 ? free * 100.0 / total : 0))
            ));
        }
        return list;
    }
}
