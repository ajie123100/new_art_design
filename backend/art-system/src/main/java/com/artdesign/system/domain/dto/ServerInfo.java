package com.artdesign.system.domain.dto;

import java.util.List;

public record ServerInfo(
        Cpu cpu,
        Memory memory,
        Jvm jvm,
        Sys sys,
        List<SysFile> sysFile
) {
    public record Cpu(
            double load,
            int count
    ) {
    }

    public record Memory(
            long total,
            long used,
            long free,
            double usedPercent
    ) {
    }

    public record Jvm(
            String name,
            String version,
            long total,
            long used,
            long free,
            double usedPercent,
            String inputDir,
            String start,
            long runTime
    ) {
    }

    public record Sys(
            String hostname,
            String osname,
            String userDir
    ) {
    }

    public record SysFile(
            String dir,
            long total,
            long used,
            long free,
            double freePercent
    ) {
    }
}
