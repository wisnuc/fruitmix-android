package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public abstract class AbstractRemoteFile extends AbstractFile {

    public static final String ALL_CAN_VIEW = "*";

    private String uuid;
    private List<String> owners = new ArrayList<>();
    private List<String> writeList = new ArrayList<>();
    private List<String> readList = new ArrayList<>();

    private String parentFolderUUID;
    private String parentFolderName;

    private String rootFolderUUID;

    private String parentFolderPath = "";

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

    public String getParentFolderUUID() {
        return parentFolderUUID;
    }

    public void setParentFolderUUID(String parentFolderUUID) {
        this.parentFolderUUID = parentFolderUUID;
    }

    public String getParentFolderName() {
        return parentFolderName;
    }

    public void setParentFolderName(String parentFolderName) {
        this.parentFolderName = parentFolderName;
    }

    public String getRootFolderUUID() {
        return rootFolderUUID;
    }

    public void setRootFolderUUID(String rootFolderUUID) {
        this.rootFolderUUID = rootFolderUUID;
    }

    public String getParentFolderPath() {
        return parentFolderPath;
    }

    public void setParentFolderPath(String parentFolderPath) {
        this.parentFolderPath = parentFolderPath;
    }

    public File getDownloadedFile() {
        return new File(FileUtil.getDownloadFileStoreFolderPath() + getParentFolderPath(), getName());
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o instanceof AbstractRemoteFile) {
            return ((AbstractRemoteFile) o).getUuid().equals(this.getUuid());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }
}
