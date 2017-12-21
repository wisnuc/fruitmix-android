package com.winsun.fruitmix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.HandleTorrentDialogViewBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.upload.InjectUploadFileCase;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.init.system.InitSystem;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.retrieve.file.from.other.app.RetrieveFileFromOtherAppUseCase;
import com.winsun.fruitmix.retrieve.file.from.other.app.RetrieveFilePresenter;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.torrent.view.TorrentDownloadManageActivity;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class TestReceiveActivity extends AppCompatActivity {

    public static final String TAG = TestReceiveActivity.class.getSimpleName();

    public static final String UPLOAD_FILE_PATH = "login_for_upload_file_path";

    public static final String UPLOAD_OR_DOWNLOAD_TORRENT = "upload_or_download_torrent";

    public static final int UPLOAD_FILE = 1;
    public static final int DOWNLOAD_TORRENT = 2;

    public static final String UPLOAD_FILE_SOURCE_FROM_APP_NAME = "upload_file_source_from_app_name";

    public static final int EQUIPMENT_SEARCH_REQUEST_CODE = 0x1001;

    public static final long MAX_UPLOAD_FILE_SIZE = 1024 * 1024 * 1024;

    private Context mContext;

    private RetrieveFilePresenter mRetrieveFilePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        RetrieveFileFromOtherAppUseCase retrieveFileFromOtherAppUseCase = new RetrieveFileFromOtherAppUseCase();

        mRetrieveFilePresenter = new RetrieveFilePresenter();

        Intent intent = getIntent();

        String uploadFilePath = retrieveFileFromOtherAppUseCase.getUploadFilePath(intent);

        if (uploadFilePath != null) {

            File file = new File(uploadFilePath);

            if (file.isFile() && file.length() > MAX_UPLOAD_FILE_SIZE) {

                Toast.makeText(mContext, "暂不支持超过1G文件上传", Toast.LENGTH_SHORT).show();

                return;
            }

            LoginUseCase loginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

            if (checkAppIsRunning(loginUseCase)) {

//                finishCurrentRunningActivity();

                mRetrieveFilePresenter.handleUploadFilePath(uploadFilePath,this);

            } else {

                MobclickAgent.setDebugMode(true);
                MobclickAgent.openActivityDurationTrack(false);
                MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

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

                mRetrieveFilePresenter.handleUploadFilePath(uploadFilePath,TestReceiveActivity.this);

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
