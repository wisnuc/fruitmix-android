package com.winsun.fruitmix.fileModule.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public abstract class AbstractRemoteFile {

    private String uuid;
    private String name;
    private List<String> owners;
    private String time;
    private String size;

    public AbstractRemoteFile(){
        owners = new ArrayList<>();
    }

    public abstract boolean isFolder();

    public abstract void openAbstractRemoteFile(Context context);

    public abstract List<AbstractRemoteFile> listChildAbstractRemoteFileList();

    public abstract void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addOwner(String owner){
        owners.add(owner);
    }

    public void removeOwner(String owner){
        owners.remove(owner);
    }

    private List<String> getOwners(){
        return Collections.unmodifiableList(owners);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
