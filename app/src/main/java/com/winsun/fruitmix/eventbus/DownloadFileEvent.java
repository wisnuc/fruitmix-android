package com.winsun.fruitmix.eventbus;


import com.winsun.fruitmix.file.data.download.FileDownloadState;

/**
 * Created by Administrator on 2016/11/7.
 */

public class DownloadFileEvent {

    private FileDownloadState fileDownloadState;

    public DownloadFileEvent(FileDownloadState fileDownloadState){
        this.fileDownloadState = fileDownloadState;
    }

    public FileDownloadState getFileDownloadState() {
        return fileDownloadState;
    }
}
