package com.artdesign.system.domain.dto;

import java.util.List;

public class RouteMeta {
    private String title;
    private String icon;
    private Boolean keepAlive;
    private Boolean fixedTab;
    private Boolean isHide;
    private Boolean isHideTab;
    private List<String> roles;
    private List<AuthButton> authList;

    public RouteMeta() {
    }

    public RouteMeta(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public static RouteMeta of(String title, String icon) {
        return new RouteMeta(title, icon);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getFixedTab() {
        return fixedTab;
    }

    public void setFixedTab(Boolean fixedTab) {
        this.fixedTab = fixedTab;
    }

    public Boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(Boolean hide) {
        isHide = hide;
    }

    public Boolean getIsHideTab() {
        return isHideTab;
    }

    public void setIsHideTab(Boolean hideTab) {
        isHideTab = hideTab;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<AuthButton> getAuthList() {
        return authList;
    }

    public void setAuthList(List<AuthButton> authList) {
        this.authList = authList;
    }

    public record AuthButton(String title, String authMark) {
    }
}
