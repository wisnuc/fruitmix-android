package com.winsun.fruitmix.fileModule.download;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadPendingState extends FileDownloadState {

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.PENDING;
    }

}
