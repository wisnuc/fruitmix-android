package com.winsun.fruitmix.eventbus;


import com.winsun.fruitmix.file.data.download.DownloadState;

/**
 * Created by Administrator on 2016/11/8.
 */

public class DownloadStateChangedEvent {

    private DownloadState downloadState;

    public DownloadStateChangedEvent(DownloadState downloadState) {
        this.downloadState = downloadState;
    }

    public DownloadState getDownloadState() {
        return downloadState;
    }

}
