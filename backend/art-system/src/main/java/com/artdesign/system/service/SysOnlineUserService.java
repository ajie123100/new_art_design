package com.artdesign.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.system.domain.dto.OnlineUser;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SysOnlineUserService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<OnlineUser> list() {
        List<OnlineUser> result = new ArrayList<>();
        if (StpUtil.isLogin()) {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            result.add(new OnlineUser(
                    (long) System.identityHashCode(loginId),
                    loginId != null ? loginId.toString() : "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    FMT.format(Instant.now())
            ));
        }
        return result;
    }

    public void forceLogout(Object loginId) {
        StpUtil.logout(loginId);
    }
}
