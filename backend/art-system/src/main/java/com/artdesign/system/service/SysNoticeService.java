package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.NoticeListItem;
import com.artdesign.system.domain.dto.NoticeSaveRequest;
import com.artdesign.system.domain.entity.SysNotice;
import com.artdesign.system.mapper.SysNoticeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysNoticeService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysNoticeMapper noticeMapper;

    public SysNoticeService(SysNoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public PageResult<NoticeListItem> list(Map<String, String> params) {
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);

        Page<SysNotice> page = new Page<>(current, size);
        IPage<SysNotice> result = noticeMapper.selectPage(page, new LambdaQueryWrapper<SysNotice>()
                .like(hasText(params.get("noticeTitle")), SysNotice::getNoticeTitle, params.get("noticeTitle"))
                .eq(hasText(params.get("noticeType")), SysNotice::getNoticeType, params.get("noticeType"))
                .eq(hasText(params.get("status")), SysNotice::getStatus, params.get("status"))
                .orderByDesc(SysNotice::getNoticeId));

        List<NoticeListItem> records = result.getRecords().stream()
                .map(this::toListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public SysNotice get(Long noticeId) {
        return findNotice(noticeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(NoticeSaveRequest request) {
        SysNotice notice = new SysNotice();
        fillNotice(notice, request);
        notice.setStatus(defaultIfBlank(request.status(), "0"));
        notice.setCreateBy("system");
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return notice.getNoticeId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(NoticeSaveRequest request) {
        if (request.noticeId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        SysNotice notice = findNotice(request.noticeId());
        fillNotice(notice, request);
        notice.setStatus(defaultIfBlank(request.status(), "0"));
        notice.setUpdateBy("system");
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            throw new BusinessException("请选择要删除的公告");
        }
        for (Long noticeId : noticeIds) {
            findNotice(noticeId);
        }
        noticeMapper.deleteBatchIds(noticeIds);
    }

    private SysNotice findNotice(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    private NoticeListItem toListItem(SysNotice notice) {
        return new NoticeListItem(
                notice.getNoticeId(),
                notice.getNoticeTitle(),
                notice.getNoticeType(),
                notice.getStatus(),
                formatDateTime(notice.getCreateTime())
        );
    }

    private void fillNotice(SysNotice notice, NoticeSaveRequest request) {
        notice.setNoticeTitle(request.noticeTitle());
        notice.setNoticeType(request.noticeType());
        notice.setNoticeContent(defaultIfBlank(request.noticeContent(), ""));
        notice.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}
