package com.winsun.fruitmix.executor;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.interfaces.FileDownloadUploadInterface;
import com.winsun.fruitmix.http.retrofit.RetrofitInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.util.concurrent.Callable;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Administrator on 2016/11/3.
 */

public class DownloadFileTask implements Callable<Boolean> {

    public static final String TAG = DownloadFileTask.class.getSimpleName();

    private FileDownloadState fileDownloadState;

    private DBUtils dbUtils;

    public DownloadFileTask(FileDownloadState fileDownloadState, DBUtils dbUtils) {
        this.fileDownloadState = fileDownloadState;
        this.dbUtils = dbUtils;
    }

    @Override
    public Boolean call() throws Exception {

        //TODO:add file state(downloading,pending,finishing.etc) and scheduler,use state mode and do function:1.log child node 2.log parent node 3.find node and return

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance().create(FileDownloadUploadInterface.class);

        String downloadFileUrl = FNAS.getDownloadFileUrl(fileDownloadState.getFileUUID(), fileDownloadState.getParentFolderUUID());

        Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(downloadFileUrl);

        Log.d(TAG, "call: fileDownloadInterface downloadFile");

        boolean result = FileUtil.writeResponseBodyToFolder(call.execute().body(), fileDownloadState);

        Log.d(TAG, "call: download result:" + result);

        if (result) {

            FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

            fileDownloadItem.setFileTime(System.currentTimeMillis());
            fileDownloadItem.setFileCreatorUUID(FNAS.userUUID);
            dbUtils.insertDownloadedFile(fileDownloadItem);
        }

        return result;
    }

}

