package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class DictDataExcel {
    @ExcelProperty("字典编码")
    public Long dictCode;
    @ExcelProperty("显示顺序")
    public Integer dictSort;
    @ExcelProperty("字典标签")
    public String dictLabel;
    @ExcelProperty("字典键值")
    public String dictValue;
    @ExcelProperty("字典类型")
    public String dictType;
    @ExcelProperty("样式属性")
    public String cssClass;
    @ExcelProperty("表格样式")
    public String listClass;
    @ExcelProperty("默认")
    public String defaultValue;
    @ExcelProperty("启用")
    public String enabled;
    @ExcelProperty("备注")
    public String remark;

    public Long getDictCode() { return dictCode; }
    public void setDictCode(Long dictCode) { this.dictCode = dictCode; }
    public Integer getDictSort() { return dictSort; }
    public void setDictSort(Integer dictSort) { this.dictSort = dictSort; }
    public String getDictLabel() { return dictLabel; }
    public void setDictLabel(String dictLabel) { this.dictLabel = dictLabel; }
    public String getDictValue() { return dictValue; }
    public void setDictValue(String dictValue) { this.dictValue = dictValue; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getCssClass() { return cssClass; }
    public void setCssClass(String cssClass) { this.cssClass = cssClass; }
    public String getListClass() { return listClass; }
    public void setListClass(String listClass) { this.listClass = listClass; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
