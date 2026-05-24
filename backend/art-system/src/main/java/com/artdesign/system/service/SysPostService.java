package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.PostListItem;
import com.artdesign.system.domain.dto.PostSaveRequest;
import com.artdesign.system.domain.entity.SysPost;
import com.artdesign.system.mapper.SysPostMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SysPostService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysPostMapper postMapper;

    public SysPostService(SysPostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public PageResult<PostListItem> list(Map<String, String> params) {
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);

        Page<SysPost> page = new Page<>(current, size);
        IPage<SysPost> result = postMapper.selectPage(page, new LambdaQueryWrapper<SysPost>()
                .like(hasText(params.get("postName")), SysPost::getPostName, params.get("postName"))
                .like(hasText(params.get("postCode")), SysPost::getPostCode, params.get("postCode"))
                .eq(hasText(params.get("status")), SysPost::getStatus, params.get("status"))
                .eq(SysPost::getDelFlag, "0")
                .orderByAsc(SysPost::getPostSort)
                .orderByAsc(SysPost::getPostId));

        List<PostListItem> records = result.getRecords().stream()
                .map(this::toListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public PostListItem get(Long postId) {
        return toListItem(findPost(postId));
    }

    public List<PostListItem> listAll() {
        return postMapper.selectList(new LambdaQueryWrapper<SysPost>()
                        .eq(SysPost::getStatus, "1")
                        .eq(SysPost::getDelFlag, "0")
                        .orderByAsc(SysPost::getPostSort))
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(PostSaveRequest request) {
        ensureUniqueCode(request.postCode(), null);
        SysPost post = new SysPost();
        fillPost(post, request);
        post.setPostSort(nextSort());
        post.setStatus(defaultStatus(request.enabled()));
        post.setDelFlag("0");
        post.setRemark(defaultIfBlank(request.remark(), ""));
        post.setCreateBy("system");
        post.setCreateTime(LocalDateTime.now());
        postMapper.insert(post);
        return post.getPostId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PostSaveRequest request) {
        if (request.postId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        SysPost post = findPost(request.postId());
        ensureUniqueCode(request.postCode(), request.postId());
        fillPost(post, request);
        post.setStatus(defaultStatus(request.enabled()));
        post.setRemark(defaultIfBlank(request.remark(), ""));
        post.setUpdateBy("system");
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            throw new BusinessException("请选择要删除的岗位");
        }
        for (Long postId : postIds) {
            SysPost post = findPost(postId);
            Long userCount = postMapper.countUsersByPostId(postId);
            if (userCount != null && userCount > 0) {
                throw new BusinessException("岗位[" + post.getPostName() + "]已分配用户，不能删除");
            }
            post.setDelFlag("1");
            post.setUpdateBy("system");
            post.setUpdateTime(LocalDateTime.now());
            postMapper.updateById(post);
        }
    }

    private SysPost findPost(Long postId) {
        SysPost post = postMapper.selectById(postId);
        if (post == null || !Objects.equals(post.getDelFlag(), "0")) {
            throw new BusinessException("岗位不存在");
        }
        return post;
    }

    private PostListItem toListItem(SysPost post) {
        return new PostListItem(
                post.getPostId(),
                post.getPostCode(),
                post.getPostName(),
                post.getPostSort(),
                Objects.equals(post.getStatus(), "1"),
                formatDateTime(post.getCreateTime())
        );
    }

    private void fillPost(SysPost post, PostSaveRequest request) {
        post.setPostCode(request.postCode());
        post.setPostName(request.postName());
        post.setPostSort(request.postSort() != null ? request.postSort() : 0);
    }

    private void ensureUniqueCode(String postCode, Long ignoreId) {
        SysPost existing = postMapper.selectOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostCode, postCode)
                .eq(SysPost::getDelFlag, "0")
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getPostId(), ignoreId)) {
            throw new BusinessException("岗位编码已存在");
        }
    }

    private int nextSort() {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<SysPost>().eq(SysPost::getDelFlag, "0"));
        return count.intValue() + 1;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String defaultStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
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
