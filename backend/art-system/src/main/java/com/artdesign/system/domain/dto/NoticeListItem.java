package com.artdesign.system.domain.dto;

public record NoticeListItem(
        Long noticeId,
        String noticeTitle,
        String noticeType,
        String status,
        String createTime
) {
}
