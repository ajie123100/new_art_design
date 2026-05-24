package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class ConfigExcel {
    @ExcelProperty("参数ID")
    public Long configId;
    @ExcelProperty("参数名称")
    public String configName;
    @ExcelProperty("参数键名")
    public String configKey;
    @ExcelProperty("参数键值")
    public String configValue;
    @ExcelProperty("系统内置")
    public String configType;
    @ExcelProperty("备注")
    public String remark;
    @ExcelProperty("创建时间")
    public String createTime;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getConfigType() { return configType; }
    public void setConfigType(String configType) { this.configType = configType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
