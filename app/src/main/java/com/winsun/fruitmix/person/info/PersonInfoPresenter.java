package com.winsun.fruitmix.person.info;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.winsun.fruitmix.EquipmentSearchActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/10/19.
 */

public class PersonInfoPresenter extends BindWeChatUserPresenter implements WXEntryActivity.WXEntryGetWeChatCodeCallback {

    public static final String TAG = PersonInfoPresenter.class.getSimpleName();

    private LogoutUseCase logoutUseCase;

    private LoginUseCase mLoginUseCase;

    private ThreadManager threadManager;

    private FileTool mFileTool;

    public PersonInfoPresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource,
                               PersonInfoView personInfoView, PersonInfoDataSource personInfoDataSource,
                               TokenDataSource tokenDataSource,LogoutUseCase logoutUseCase,
                               LoginUseCase loginUseCase, ThreadManager threadManager, FileTool fileTool) {

        super(userDataRepository, systemSettingDataSource, personInfoView, personInfoDataSource,tokenDataSource);
        this.logoutUseCase = logoutUseCase;
        mLoginUseCase = loginUseCase;
        this.threadManager = threadManager;
        mFileTool = fileTool;
    }

    public void onDestroy() {

        personInfoView = null;

    }


    public void logout() {

        if (mFileTool.checkTemporaryUploadFolderNotEmpty(personInfoView.getContext(), systemSettingDataSource.getCurrentLoginUserUUID())) {

            AlertDialog dialog = new AlertDialog.Builder(personInfoView.getContext())
                    .setMessage(R.string.clear_temporary_folder_before_logout_toast).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                            doLogout();

                        }
                    }).setNegativeButton(R.string.cancel, null).create();

            dialog.show();

        } else
            doLogout();

    }

    private void doLogout() {
        personInfoView.showProgressDialog(String.format(personInfoView.getString(R.string.operating_title), personInfoView.getString(R.string.logout)));

        Future<Boolean> future = threadManager.runOnCacheThread(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                logoutUseCase.logout();

                mLoginUseCase.setAlreadyLogin(false);

                return true;
            }
        });

        try {
            Boolean result = future.get();

            if (personInfoView == null)
                return;

            personInfoView.dismissDialog();

            personInfoView.setResult(NavPagerActivity.RESULT_FINISH_ACTIVITY);

            EquipmentSearchActivity.gotoEquipmentActivity((Activity) personInfoView.getContext(), true);

        } catch (InterruptedException e) {
            e.printStackTrace();

            personInfoView.dismissDialog();

            personInfoView.showToast(personInfoView.getString(R.string.fail, personInfoView.getString(R.string.logout)));


        } catch (ExecutionException e) {
            e.printStackTrace();

            personInfoView.dismissDialog();

            personInfoView.showToast(personInfoView.getString(R.string.fail, personInfoView.getString(R.string.logout)));

        }
    }


}
