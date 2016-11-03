package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.FileDownloadUploadInterface;
import com.winsun.fruitmix.retrofit.RetrofitInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2016/11/3.
 */

public class DownloadFileTask implements Callable<Boolean> {

    private String fileUUID;
    private String fileName;

    public DownloadFileTask(String fileUUID, String fileName) {
        this.fileUUID = fileUUID;
        this.fileName = fileName;
    }

    @Override
    public Boolean call() throws Exception {

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance().create(FileDownloadUploadInterface.class);

        Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(Util.FILE_PARAMETER + "/" + fileUUID);

        boolean result = FileUtil.writeResponseBodyToFolder(call.execute().body(), fileName);

        if (result) {
            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_DOWNLOAD_STATE_CHANGED, OperationResult.SUCCEED));
        } else {
            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_DOWNLOAD_STATE_CHANGED, OperationResult.FAIL));
        }

        return result;
    }

}

