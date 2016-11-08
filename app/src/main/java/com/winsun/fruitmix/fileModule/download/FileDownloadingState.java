package com.winsun.fruitmix.fileModule.download;

import com.winsun.fruitmix.eventbus.DownloadFileEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadingState extends FileDownloadState {
    public FileDownloadingState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.DOWNLOADING;
    }

    @Override
    public void startWork() {
        EventBus.getDefault().post(new DownloadFileEvent(this));
    }


}
