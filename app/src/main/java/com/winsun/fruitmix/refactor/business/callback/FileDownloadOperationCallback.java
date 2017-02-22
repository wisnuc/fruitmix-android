package com.winsun.fruitmix.refactor.business.callback;

import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;

import java.util.List;

/**
 * Created by Administrator on 2017/2/22.
 */

public interface FileDownloadOperationCallback {

    interface LoadDownloadedFilesCallback{

        void onLoaded(List<FileDownloadItem> fileDownloadItems);

    }

    interface FileDownloadStateChangedCallback{

        void onStateChanged(DownloadState state);

    }

    interface DeleteDownloadedFilesCallback{

        void onFinished();

    }

}
