package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.DictDataListItem;
import com.artdesign.system.domain.dto.DictDataSaveRequest;
import com.artdesign.system.domain.dto.DictDataStatusRequest;
import com.artdesign.system.domain.dto.DictTypeListItem;
import com.artdesign.system.domain.dto.DictTypeSaveRequest;
import com.artdesign.system.domain.dto.DictTypeStatusRequest;
import com.artdesign.system.domain.entity.SysDictData;
import com.artdesign.system.domain.entity.SysDictType;
import com.artdesign.system.mapper.SysDictDataMapper;
import com.artdesign.system.mapper.SysDictTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SysDictService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public SysDictService(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    public PageResult<DictTypeListItem> listDictTypes(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<DictTypeListItem> records = dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                        .like(hasText(params.get("dictName")), SysDictType::getDictName, params.get("dictName"))
                        .like(hasText(params.get("dictType")), SysDictType::getDictType, params.get("dictType"))
                        .eq(hasText(params.get("enabled")), SysDictType::getStatus, booleanToStatus(params.get("enabled")))
                        .orderByAsc(SysDictType::getDictId))
                .stream()
                .map(this::toDictTypeListItem)
                .toList();
        return page(records, current, size);
    }

    public DictTypeListItem getDictType(Long dictId) {
        return toDictTypeListItem(findDictType(dictId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDictType(DictTypeSaveRequest request) {
        ensureUniqueDictType(request.dictType(), null);
        SysDictType dictType = new SysDictType();
        fillDictType(dictType, request);
        dictType.setCreateBy("system");
        dictType.setCreateTime(LocalDateTime.now());
        dictTypeMapper.insert(dictType);
        return dictType.getDictId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(DictTypeSaveRequest request) {
        if (request.dictId() == null) {
            throw new BusinessException("字典ID不能为空");
        }
        SysDictType dictType = findDictType(request.dictId());
        String oldDictType = dictType.getDictType();
        ensureUniqueDictType(request.dictType(), request.dictId());
        fillDictType(dictType, request);
        dictType.setUpdateBy("system");
        dictType.setUpdateTime(LocalDateTime.now());
        dictTypeMapper.updateById(dictType);

        if (!Objects.equals(oldDictType, dictType.getDictType())) {
            updateDictDataType(oldDictType, dictType.getDictType());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictTypes(List<Long> dictIds) {
        if (dictIds == null || dictIds.isEmpty()) {
            throw new BusinessException("请选择要删除的字典类型");
        }
        for (Long dictId : dictIds) {
            SysDictType dictType = findDictType(dictId);
            Long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType.getDictType()));
            if (dataCount > 0) {
                throw new BusinessException("字典类型已分配数据，不能删除");
            }
            dictTypeMapper.deleteById(dictId);
        }
    }

    public void updateDictTypeStatus(DictTypeStatusRequest request) {
        SysDictType dictType = findDictType(request.dictId());
        dictType.setStatus(booleanToStatus(request.enabled()));
        dictType.setUpdateBy("system");
        dictType.setUpdateTime(LocalDateTime.now());
        dictTypeMapper.updateById(dictType);
    }

    public PageResult<DictDataListItem> listDictData(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<DictDataListItem> records = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(hasText(params.get("dictType")), SysDictData::getDictType, params.get("dictType"))
                        .like(hasText(params.get("dictLabel")), SysDictData::getDictLabel, params.get("dictLabel"))
                        .eq(hasText(params.get("enabled")), SysDictData::getStatus, booleanToStatus(params.get("enabled")))
                        .orderByAsc(SysDictData::getDictSort)
                        .orderByAsc(SysDictData::getDictCode))
                .stream()
                .map(this::toDictDataListItem)
                .toList();
        return page(records, current, size);
    }

    public List<DictDataListItem> listDictDataByType(String dictType) {
        if (!hasText(dictType)) {
            throw new BusinessException("字典类型不能为空");
        }
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getStatus, "1")
                        .orderByAsc(SysDictData::getDictSort)
                        .orderByAsc(SysDictData::getDictCode))
                .stream()
                .map(this::toDictDataListItem)
                .toList();
    }

    public DictDataListItem getDictData(Long dictCode) {
        return toDictDataListItem(findDictData(dictCode));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDictData(DictDataSaveRequest request) {
        ensureDictTypeExists(request.dictType());
        SysDictData dictData = new SysDictData();
        fillDictData(dictData, request);
        dictData.setCreateBy("system");
        dictData.setCreateTime(LocalDateTime.now());
        dictDataMapper.insert(dictData);
        return dictData.getDictCode();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(DictDataSaveRequest request) {
        if (request.dictCode() == null) {
            throw new BusinessException("字典编码不能为空");
        }
        ensureDictTypeExists(request.dictType());
        SysDictData dictData = findDictData(request.dictCode());
        fillDictData(dictData, request);
        dictData.setUpdateBy("system");
        dictData.setUpdateTime(LocalDateTime.now());
        dictDataMapper.updateById(dictData);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(List<Long> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            throw new BusinessException("请选择要删除的字典数据");
        }
        for (Long dictCode : dictCodes) {
            findDictData(dictCode);
            dictDataMapper.deleteById(dictCode);
        }
    }

    public void updateDictDataStatus(DictDataStatusRequest request) {
        SysDictData dictData = findDictData(request.dictCode());
        dictData.setStatus(booleanToStatus(request.enabled()));
        dictData.setUpdateBy("system");
        dictData.setUpdateTime(LocalDateTime.now());
        dictDataMapper.updateById(dictData);
    }

    private SysDictType findDictType(Long dictId) {
        SysDictType dictType = dictTypeMapper.selectById(dictId);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        return dictType;
    }

    private SysDictData findDictData(Long dictCode) {
        SysDictData dictData = dictDataMapper.selectById(dictCode);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }
        return dictData;
    }

    private void ensureUniqueDictType(String dictType, Long ignoredDictId) {
        SysDictType existing = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getDictId(), ignoredDictId)) {
            throw new BusinessException("字典类型已存在");
        }
    }

    private void ensureDictTypeExists(String dictType) {
        Long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType));
        if (count == 0) {
            throw new BusinessException("字典类型不存在");
        }
    }

    private void updateDictDataType(String oldDictType, String newDictType) {
        List<SysDictData> dictDataList = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, oldDictType));
        for (SysDictData dictData : dictDataList) {
            dictData.setDictType(newDictType);
            dictData.setUpdateBy("system");
            dictData.setUpdateTime(LocalDateTime.now());
            dictDataMapper.updateById(dictData);
        }
    }

    private void fillDictType(SysDictType dictType, DictTypeSaveRequest request) {
        dictType.setDictName(request.dictName());
        dictType.setDictType(request.dictType());
        dictType.setStatus(booleanToStatus(request.enabled()));
        dictType.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private void fillDictData(SysDictData dictData, DictDataSaveRequest request) {
        dictData.setDictSort(request.dictSort());
        dictData.setDictLabel(request.dictLabel());
        dictData.setDictValue(request.dictValue());
        dictData.setDictType(request.dictType());
        dictData.setCssClass(defaultIfBlank(request.cssClass(), ""));
        dictData.setListClass(defaultIfBlank(request.listClass(), "default"));
        dictData.setIsDefault(Boolean.TRUE.equals(request.defaultValue()) ? "Y" : "N");
        dictData.setStatus(booleanToStatus(request.enabled()));
        dictData.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private DictTypeListItem toDictTypeListItem(SysDictType dictType) {
        return new DictTypeListItem(
                dictType.getDictId(),
                dictType.getDictName(),
                dictType.getDictType(),
                Objects.equals(dictType.getStatus(), "1"),
                defaultIfBlank(dictType.getRemark(), ""),
                formatDateTime(dictType.getCreateTime()),
                formatDateTime(dictType.getUpdateTime())
        );
    }

    private DictDataListItem toDictDataListItem(SysDictData dictData) {
        return new DictDataListItem(
                dictData.getDictCode(),
                dictData.getDictSort(),
                dictData.getDictLabel(),
                dictData.getDictValue(),
                dictData.getDictType(),
                defaultIfBlank(dictData.getCssClass(), ""),
                defaultIfBlank(dictData.getListClass(), "default"),
                Objects.equals(dictData.getIsDefault(), "Y"),
                Objects.equals(dictData.getStatus(), "1"),
                defaultIfBlank(dictData.getRemark(), ""),
                formatDateTime(dictData.getCreateTime()),
                formatDateTime(dictData.getUpdateTime())
        );
    }

    private <T> PageResult<T> page(List<T> records, long current, long size) {
        int from = (int) Math.min(Math.max(current - 1, 0) * size, records.size());
        int to = (int) Math.min(from + size, records.size());
        return PageResult.of(records.subList(from, to), current, size, records.size());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String booleanToStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
    }

    private String booleanToStatus(String enabled) {
        return Objects.equals(enabled, "true") ? "1" : "2";
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
