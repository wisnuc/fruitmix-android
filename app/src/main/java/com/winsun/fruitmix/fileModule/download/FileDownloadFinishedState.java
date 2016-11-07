package com.winsun.fruitmix.fileModule.download;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadFinishedState extends FileDownloadState {

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.FINISHED;
    }

}
