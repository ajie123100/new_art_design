package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class UserExcel {
    @ExcelProperty("用户ID")
    public Long userId;
    @ExcelProperty("用户名")
    public String userName;
    @ExcelProperty("昵称")
    public String nickName;
    @ExcelProperty("手机号")
    public String userPhone;
    @ExcelProperty("邮箱")
    public String userEmail;
    @ExcelProperty("性别")
    public String userGender;
    @ExcelProperty("状态")
    public String status;
    @ExcelProperty("密码")
    public String password;
    @ExcelProperty("角色编码")
    public String userRoles;
    @ExcelProperty("创建时间")
    public String createTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserGender() { return userGender; }
    public void setUserGender(String userGender) { this.userGender = userGender; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserRoles() { return userRoles; }
    public void setUserRoles(String userRoles) { this.userRoles = userRoles; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
