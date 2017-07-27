package com.winsun.fruitmix.file.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFile extends AbstractRemoteFile {

    public RemoteFile() {

        setFileTypeResID(R.drawable.file_icon);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean openAbstractRemoteFile(Context context,String rootUUID) {

        return FileUtil.openAbstractRemoteFile(context, getName());

    }

    @Override
    public List<AbstractRemoteFile> listChildAbstractRemoteFileList() {
        throw new UnsupportedOperationException("File can not call this operation");
    }

    @Override
    public void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles) {
        throw new UnsupportedOperationException("File can not call this operation");
    }

    @Override
    public String getTimeDateText() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss上传").format(new Date(Long.parseLong(getTime())));
    }

}
