package com.winsun.fruitmix.modify.user;

import android.app.Activity;
import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.BaseOperateUserPresenter;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/10/19.
 */

public class ModifyUserPresenter extends BaseOperateUserPresenter {

    private ModifyUserView modifyUserView;

    public ModifyUserPresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource, ModifyUserView modifyUserView) {
        super(userDataRepository, systemSettingDataSource);
        this.modifyUserView = modifyUserView;
    }

    public void onDestroy() {
        modifyUserView = null;
    }

    public void modifyUserName(final Context context, OperateUserViewModel operateUserViewModel, String userUUID, String originalUserName) {

        modifyUserView.hideSoftInput();

        if (!Util.isNetworkConnected(context)) {
            modifyUserView.showToast(context.getString(R.string.no_network));
            return;
        }

        final String userName = operateUserViewModel.getUserName();

        if (originalUserName.equals(userName)) {

            modifyUserView.finishView();

            return;
        }

        if (!checkOperateUserName(context, userName, operateUserViewModel))
            return;

        modifyUserView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.modify_user_name)));

        mUserDataRepository.modifyUserName(userUUID, userName, new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

                modifyUserView.dismissDialog();

                modifyUserView.setResultCode(Activity.RESULT_OK);

                modifyUserView.showToast(context.getString(R.string.success, context.getString(R.string.modify_user_name)));

                modifyUserView.finishView();

            }

            @Override
            public void onFail(OperationResult result) {

                modifyUserView.dismissDialog();

                modifyUserView.showToast(result.getResultMessage(context));

            }
        });

    }

    public void modifyUserPassword(final Context context, ModifyUserPasswordViewModel modifyUserPasswordViewModel, String userUUID) {

        modifyUserView.hideSoftInput();

        if (!Util.isNetworkConnected(context)) {
            modifyUserView.showToast(context.getString(R.string.no_network));
            return;
        }

        String originalUserPassword = modifyUserPasswordViewModel.getUserOriginalPassword();
        String newPassword = modifyUserPasswordViewModel.getUserPassword();
        String confirmPassword = modifyUserPasswordViewModel.getUserConfirmPassword();

        if (!checkOperateUserPassword(context, newPassword, confirmPassword, modifyUserPasswordViewModel))
            return;

        if (originalUserPassword.equals(newPassword)) {

            modifyUserView.finishView();

            return;

        }

        modifyUserView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.modify_password)));

        mUserDataRepository.modifyUserPassword(userUUID, originalUserPassword, newPassword, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                modifyUserView.dismissDialog();

                modifyUserView.showToast(context.getString(R.string.success, context.getString(R.string.modify_password)));

                modifyUserView.finishView();

            }

            @Override
            public void onFail(OperationResult result) {

                modifyUserView.dismissDialog();

                modifyUserView.showToast(result.getResultMessage(context));

            }
        });

    }


}
