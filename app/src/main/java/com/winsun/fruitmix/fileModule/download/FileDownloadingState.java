package com.winsun.fruitmix.fileModule.download;

import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.DownloadFileEvent;
import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadingState extends FileDownloadState {

    public static final String TAG = FileDownloadingState.class.getSimpleName();

    public FileDownloadingState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.DOWNLOADING;
    }

    @Override
    public void startWork() {
        Log.d(TAG, "startWork: ");
    }

}
