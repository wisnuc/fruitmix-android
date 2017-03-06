package com.winsun.fruitmix.data.dataOperationResult;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;

import java.util.List;

/**
 * Created by Administrator on 2017/2/27.
 */

public class FileDownloadLoadOperationResult extends DataOperationResult {

    private List<FileDownloadItem> fileDownloadItems;

    public List<FileDownloadItem> getFileDownloadItems() {
        return fileDownloadItems;
    }

    public void setFileDownloadItems(List<FileDownloadItem> fileDownloadItems) {
        this.fileDownloadItems = fileDownloadItems;
    }
}
