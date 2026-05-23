package com.artdesign.system.service;

import com.artdesign.system.domain.entity.SysOperLog;
import com.artdesign.system.mapper.SysOperLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogService {
    private final SysOperLogMapper operLogMapper;

    public SysOperLogService(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    public void record(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }
}
