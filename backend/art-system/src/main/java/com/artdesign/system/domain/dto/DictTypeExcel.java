package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class DictTypeExcel {
    @ExcelProperty("字典ID")
    public Long dictId;
    @ExcelProperty("字典名称")
    public String dictName;
    @ExcelProperty("字典类型")
    public String dictType;
    @ExcelProperty("启用")
    public String enabled;
    @ExcelProperty("备注")
    public String remark;
    @ExcelProperty("创建时间")
    public String createTime;

    public Long getDictId() { return dictId; }
    public void setDictId(Long dictId) { this.dictId = dictId; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
