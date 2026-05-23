package com.artdesign.system.domain.dto;

public record PostListItem(
        Long postId,
        String postCode,
        String postName,
        Integer postSort,
        boolean enabled,
        String createTime
) {
}
