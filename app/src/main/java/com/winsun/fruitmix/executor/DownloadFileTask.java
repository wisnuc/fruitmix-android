package com.winsun.fruitmix.executor;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
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

    private Context context;

    public DownloadFileTask(FileDownloadState fileDownloadState, Context context) {
        this.fileDownloadState = fileDownloadState;
        this.context = context;
    }

    @Override
    public Boolean call() throws Exception {

        //TODO:add file state(downloading,pending,finishing.etc) and scheduler,use state mode and do function:1.log child node 2.log parent node 3.find node and return

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance().create(FileDownloadUploadInterface.class);

        Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(FNAS.Gateway + ":" + FNAS.PORT + Util.FILE_PARAMETER + "/" + fileDownloadState.getFileUUID());

        Log.i(TAG, "call: fileDownloadInterface downloadFile");

        boolean result = FileUtil.writeResponseBodyToFolder(call.execute().body(), fileDownloadState);

        Log.i(TAG, "call: download result:" + result);

        if (result) {
            DBUtils dbUtils = DBUtils.getInstance(context);

            FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

            fileDownloadItem.setFileTime(System.currentTimeMillis());
            dbUtils.insertDownloadedFile(fileDownloadItem);
        }

        return result;
    }

}

