package com.winsun.fruitmix.fileModule.model;

/**
 * Created by Administrator on 2016/10/25.
 */

public abstract class AbstractLocalFile {

    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public abstract boolean isFolder();

}
