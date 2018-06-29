package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LocalFolder extends AbstractLocalFile {

    public List<AbstractLocalFile> contents;

    public LocalFolder() {

        setFileTypeResID(R.drawable.folder_icon);

    }

    @Override
    public boolean isFolder() {
        return true;
    }

    public void addContent(AbstractLocalFile file) {
        contents.add(file);
    }

    public void removeContent(AbstractLocalFile file) {
        contents.remove(file);
    }

    public List<AbstractLocalFile> getContents() {
        return contents;
    }

    @Override
    public AbstractFile copySelf() {

        LocalFolder localFolder = new LocalFolder();
        localFolder.setName(getName());
        localFolder.setFileTypeResID(getFileTypeResID());
        localFolder.setSize(getSize());
        localFolder.setTime(getTime());
        localFolder.setPath(getPath());

        return localFolder;

    }
}
