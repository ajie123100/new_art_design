package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.DictDataExcel;
import com.artdesign.system.domain.dto.DictDataListItem;
import com.artdesign.system.domain.dto.DictDataSaveRequest;
import com.artdesign.system.domain.dto.DictDataStatusRequest;
import com.artdesign.system.domain.dto.DictTypeExcel;
import com.artdesign.system.domain.dto.DictTypeListItem;
import com.artdesign.system.domain.dto.DictTypeSaveRequest;
import com.artdesign.system.domain.dto.DictTypeStatusRequest;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.service.SysDictService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict")
public class SysDictController {
    private final SysDictService dictService;

    public SysDictController(SysDictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/type/list")
    @SaCheckPermission("system:dict:list")
    public R<PageResult<DictTypeListItem>> listTypes(@RequestParam Map<String, String> params) {
        return R.ok(dictService.listDictTypes(params));
    }

    @GetMapping("/type/{id}")
    @SaCheckPermission("system:dict:list")
    public R<DictTypeListItem> getType(@PathVariable Long id) {
        return R.ok(dictService.getDictType(id));
    }

    @GetMapping("/type/export")
    @SaCheckPermission("system:dict:export")
    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    public void exportTypes(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, dictService.exportDictTypes(params), DictTypeExcel.class, "dict_type");
    }

    @PostMapping("/type/import")
    @SaCheckPermission("system:dict:import")
    @Log(title = "字典类型", businessType = BusinessType.IMPORT)
    public R<ImportResult> importTypes(@RequestPart("file") Part file) throws IOException {
        return R.ok(dictService.importDictTypes(ExcelUtil.readExcel(file, DictTypeExcel.class)));
    }

    @PostMapping("/type")
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    public R<Long> createType(@Valid @RequestBody DictTypeSaveRequest request) {
        return R.ok(dictService.createDictType(request));
    }

    @PutMapping("/type")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public R<Void> updateType(@Valid @RequestBody DictTypeSaveRequest request) {
        dictService.updateDictType(request);
        return R.ok();
    }

    @DeleteMapping("/type/{ids}")
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    public R<Void> deleteTypes(@PathVariable String ids) {
        dictService.deleteDictTypes(parseIds(ids));
        return R.ok();
    }

    @PutMapping("/type/status")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public R<Void> updateTypeStatus(@Valid @RequestBody DictTypeStatusRequest request) {
        dictService.updateDictTypeStatus(request);
        return R.ok();
    }

    @GetMapping("/data/list")
    @SaCheckPermission("system:dict:list")
    public R<PageResult<DictDataListItem>> listData(@RequestParam Map<String, String> params) {
        return R.ok(dictService.listDictData(params));
    }

    @GetMapping("/data/type/{dictType}")
    public R<List<DictDataListItem>> listDataByType(@PathVariable String dictType) {
        return R.ok(dictService.listDictDataByType(dictType));
    }

    @GetMapping("/data/{id}")
    @SaCheckPermission("system:dict:list")
    public R<DictDataListItem> getData(@PathVariable Long id) {
        return R.ok(dictService.getDictData(id));
    }

    @GetMapping("/data/export")
    @SaCheckPermission("system:dict:export")
    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    public void exportData(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, dictService.exportDictData(params), DictDataExcel.class, "dict_data");
    }

    @PostMapping("/data/import")
    @SaCheckPermission("system:dict:import")
    @Log(title = "字典数据", businessType = BusinessType.IMPORT)
    public R<ImportResult> importData(@RequestPart("file") Part file) throws IOException {
        return R.ok(dictService.importDictData(ExcelUtil.readExcel(file, DictDataExcel.class)));
    }

    @PostMapping("/data")
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    public R<Long> createData(@Valid @RequestBody DictDataSaveRequest request) {
        return R.ok(dictService.createDictData(request));
    }

    @PutMapping("/data")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    public R<Void> updateData(@Valid @RequestBody DictDataSaveRequest request) {
        dictService.updateDictData(request);
        return R.ok();
    }

    @DeleteMapping("/data/{ids}")
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    public R<Void> deleteData(@PathVariable String ids) {
        dictService.deleteDictData(parseIds(ids));
        return R.ok();
    }

    @PutMapping("/data/status")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    public R<Void> updateDataStatus(@Valid @RequestBody DictDataStatusRequest request) {
        dictService.updateDictDataStatus(request);
        return R.ok();
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
