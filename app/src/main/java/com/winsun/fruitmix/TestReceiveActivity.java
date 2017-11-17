package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.file.data.upload.FileUploadItem;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.init.system.InitSystem;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.retrieve.file.from.other.app.RetrieveFileFromOtherAppUseCase;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class TestReceiveActivity extends AppCompatActivity {

    public static final String TAG = TestReceiveActivity.class.getSimpleName();

    public static final String UPLOAD_FILE_PATH = "login_for_upload_file_path";

    public static final String UPLOAD_FILE_SOURCE_FROM_APP_NAME = "upload_file_source_from_app_name";

    public static final int EQUIPMENT_SEARCH_REQUEST_CODE = 0x1001;

    private Context mContext;

    private String uploadFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        RetrieveFileFromOtherAppUseCase retrieveFileFromOtherAppUseCase = new RetrieveFileFromOtherAppUseCase();

        Intent intent = getIntent();

        uploadFilePath = retrieveFileFromOtherAppUseCase.getUploadFilePath(intent);

        if (uploadFilePath != null) {

            LoginUseCase loginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

            if (checkAppIsRunning(loginUseCase)) {

//                finishCurrentRunningActivity();

                startTaskManageActivity(uploadFilePath);

            } else {

                handleAppIsNotRunning(uploadFilePath);

            }

        } else {
            Toast.makeText(mContext, getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
        }

    }

    private void finishCurrentRunningActivity() {
        EventBus.getDefault().post(new OperationEvent(Util.KEY_STOP_CURRENT_ACTIVITY, new OperationSuccess()));
    }

    private void handleAppIsNotRunning(final String uploadFilePath) {
        InitSystem.initSystem(this);

        LoginUseCase loginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

        loginUseCase.loginWithNoParam(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                Log.d(TAG, "onSucceed: login with no param and start task manage activity");

                startTaskManageActivity(uploadFilePath);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: login with no param and start equipment search activity");

                Intent gotoEquipmentSearchActivityIntent = new Intent(mContext, EquipmentSearchActivity.class);
                gotoEquipmentSearchActivityIntent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, true);
                gotoEquipmentSearchActivityIntent.putExtra(UPLOAD_FILE_PATH, uploadFilePath);

                startActivity(gotoEquipmentSearchActivityIntent);

                finish();

            }
        });
    }

    private void startTaskManageActivity(String uploadFilePath) {

        Log.d(TAG, "startTaskManageActivity: uploadFilePath: " + uploadFilePath);

        startUploadFileTask(uploadFilePath,mContext);

        finish();

    }

    public static void startUploadFileTask(String uploadFilePath,Context context) {
        Intent gotoTaskManageActivityIntent = new Intent(context, FileDownloadActivity.class);
        context.startActivity(gotoTaskManageActivityIntent);

        FileTaskManager fileTaskManager = FileTaskManager.getInstance();

        File file = new File(uploadFilePath);

        String fileHash = Util.calcSHA256OfFile(uploadFilePath);

        fileTaskManager.addFileUploadItem(new FileUploadItem(fileHash,file.getName(),file.length(), uploadFilePath), context);
    }

    //TODO:consider alreadyLogin logic,init system and destroy instance will cause large gc?

    private boolean checkAppIsRunning(LoginUseCase loginUseCase) {

        boolean result = loginUseCase.isAlreadyLogin();

        Log.d(TAG, "checkAppIsRunning: " + result);

        return result;
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");
    }
}
