package com.artdesign.web.controller.system;

import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.common.core.domain.R;
import com.artdesign.system.domain.dto.AppRouteRecord;
import com.artdesign.system.service.SysMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v3/system")
public class SysMenuController {
    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    public R<List<AppRouteRecord>> menus() {
        return R.ok(menuService.getMenuTree(StpUtil.getLoginIdAsLong()));
    }
}
