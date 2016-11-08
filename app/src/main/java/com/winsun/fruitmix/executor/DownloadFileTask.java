package com.winsun.fruitmix.executor;

import android.util.Log;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
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

    public static final String TAG = DownloadFileTask.class.getSimpleName();

    private FileDownloadState fileDownloadState;

    public DownloadFileTask(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;
    }

    @Override
    public Boolean call() throws Exception {

        //TODO:add file state(downloading,pending,finishing.etc) and scheduler,use state mode and do function:1.log child node 2.log parent node 3.find node and return
        //TODO:replace eventbus poststicky function of post function for the issue:event is posted before register listener

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance().create(FileDownloadUploadInterface.class);

        Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(FNAS.Gateway + ":" + FNAS.PORT + Util.FILE_PARAMETER + "/" + fileDownloadState.getFileUUID());

        Log.i(TAG, "call: fileDownloadInterface downloadFile");

        boolean result = FileUtil.writeResponseBodyToFolder(call.execute().body(), fileDownloadState);

        Log.i(TAG, "call: download result:" + result);

        return result;
    }

}

