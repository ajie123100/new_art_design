package com.artdesign.system.domain.dto;

import java.util.List;

public class AppRouteRecord {
    private Long id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private RouteMeta meta;
    private List<AppRouteRecord> children;

    public AppRouteRecord() {
    }

    public AppRouteRecord(Long id, String path, String name, String component, RouteMeta meta) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.component = component;
        this.meta = meta;
    }

    public static AppRouteRecord route(Long id, String path, String name, String component, RouteMeta meta) {
        return new AppRouteRecord(id, path, name, component, meta);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public RouteMeta getMeta() {
        return meta;
    }

    public void setMeta(RouteMeta meta) {
        this.meta = meta;
    }

    public List<AppRouteRecord> getChildren() {
        return children;
    }

    public void setChildren(List<AppRouteRecord> children) {
        this.children = children;
    }
}
