package com.winsun.fruitmix.login;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;

/**
 * Created by Administrator on 2017/11/24.
 */

public class LoginNewUserCallbackWrapper<T> implements BaseOperateDataCallback<T> {

    public static final String TAG = LoginNewUserCallbackWrapper.class.getSimpleName();

    private BaseOperateDataCallback<T> mCallback;

    private String temporaryUploadFolderPath;

    private FileTool mFileTool;

    LoginNewUserCallbackWrapper(String temporaryUploadFolderPath,FileTool fileTool) {
        this.temporaryUploadFolderPath = temporaryUploadFolderPath;
        mFileTool = fileTool;
    }

    public void setCallback(BaseOperateDataCallback<T> callback) {
        mCallback = callback;
    }

    @Override
    public void onFail(OperationResult operationResult) {
        mCallback.onFail(operationResult);
    }

    @Override
    public void onSucceed(T data, OperationResult result) {

        boolean deleteFileInTemporaryUploadFolder = mFileTool.deleteDir(new File(temporaryUploadFolderPath));

        Log.d(TAG, "onSucceed: deleteFileInTemporaryUploadFolder: " + deleteFileInTemporaryUploadFolder);

        mCallback.onSucceed(data, result);

    }
}
