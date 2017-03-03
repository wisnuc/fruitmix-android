package com.winsun.fruitmix.fileModule.model;

import android.content.Context;

import com.winsun.fruitmix.mediaModule.model.MediaShareContent;

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
    private List<String> writeList;
    private List<String> readList;
    private String time;
    private String size;

    AbstractRemoteFile() {
        owners = new ArrayList<>();
        writeList = new ArrayList<>();
        readList = new ArrayList<>();
    }

    public abstract boolean isFolder();

    public abstract List<AbstractRemoteFile> listChildAbstractRemoteFileList();

    public abstract void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles);

    public abstract String getTimeDateText();

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

    public void addOwner(String owner) {
        owners.add(owner);
    }

    public void removeOwner(String owner) {
        owners.remove(owner);
    }

    public List<String> getOwners() {
        return Collections.unmodifiableList(owners);
    }

    public void addReadList(String reader) {
        readList.add(reader);
    }

    public List<String> getReadList() {
        return Collections.unmodifiableList(readList);
    }

    public void addWriteList(String writer) {
        writeList.add(writer);
    }

    public List<String> getWriteList() {
        return Collections.unmodifiableList(writeList);
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

    @Override
    public boolean equals(Object o) {

        if(this == o)
            return true;

        if(o instanceof AbstractRemoteFile){
            return ((AbstractRemoteFile) o).getUuid().equals(this.getUuid());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }
}
