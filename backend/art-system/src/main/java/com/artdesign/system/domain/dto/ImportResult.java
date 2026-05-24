package com.artdesign.system.domain.dto;

public record ImportResult(
        int imported,
        int skipped
) {
}
