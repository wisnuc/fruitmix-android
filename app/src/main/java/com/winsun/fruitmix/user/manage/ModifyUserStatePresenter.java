package com.winsun.fruitmix.user.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.databinding.ActivityModifyUserStateBinding;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/12/12.
 */

public class ModifyUserStatePresenter implements ActiveView {

    private UserDataRepository mUserDataRepository;

    private String modifyUserUUID;

    private BaseView mBaseView;

    private User currentUser;
    private User modifyUser;

    private ActivityModifyUserStateBinding mActivityModifyUserStateBinding;

    ModifyUserStatePresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource, String modifyUserUUID,
                             BaseView baseView, ActivityModifyUserStateBinding binding, ImageLoader imageLoader) {
        mUserDataRepository = userDataRepository;

        this.modifyUserUUID = modifyUserUUID;
        mBaseView = baseView;

        currentUser = mUserDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());

        mActivityModifyUserStateBinding = binding;

        refreshView();

        mActivityModifyUserStateBinding.userAvatar.setUser(modifyUser, imageLoader);

    }

    private void refreshView() {

        modifyUser = mUserDataRepository.getUserByUUID(modifyUserUUID);

        mActivityModifyUserStateBinding.setUser(modifyUser);

        mActivityModifyUserStateBinding.modifyUserState.setEnabled(hasEnablePermission());

        boolean canModifyUserType = !modifyUser.isFirstUser() && currentUser.isFirstUser();

        mActivityModifyUserStateBinding.modifyUserTypeIcon.setVisibility(canModifyUserType ? View.VISIBLE : View.GONE);

        mActivityModifyUserStateBinding.userTypeLayout.setEnabled(canModifyUserType);

    }

    public void onDestroy() {
        mBaseView = null;
    }

    private boolean hasEnablePermission() {

        return !modifyUser.isFirstUser() && (currentUser.isFirstUser() || currentUser.isAdmin() && !modifyUser.isAdmin());

    }

    public void modifyEnableState(final Context context) {

        boolean isDisabled = modifyUser.isDisabled();

        final String operationName;

        if (isDisabled) {
            operationName = context.getString(R.string.enable);
        } else
            operationName = context.getString(R.string.disable);

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, operationName));

        mUserDataRepository.modifyUserEnableState(modifyUserUUID, !isDisabled, new BaseOperateDataCallbackWrapper<>(new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

                mBaseView.dismissDialog();

                mBaseView.showToast(context.getString(R.string.success, operationName));

                refreshView();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mBaseView.dismissDialog();

                mBaseView.showToast(operationResult.getResultMessage(context));

            }
        }, this));


    }

    public void showModifyUserIsAdminStateDialog(final Context context) {

        String[] choiceItems = {context.getString(R.string.admin), context.getString(R.string.ordinary_user)};

        final int checkedItem = modifyUser.isAdmin() ? 0 : 1;

        new AlertDialog.Builder(context).setSingleChoiceItems(choiceItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == checkedItem)
                    return;

                dialog.dismiss();

                modifyUserIsAdminState(which == 0, context);

            }
        }).create().show();

    }

    private void modifyUserIsAdminState(boolean newIsAdminState, final Context context) {

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.modify_user_operation)));

        mUserDataRepository.modifyUserIsAdminState(modifyUserUUID, newIsAdminState, new BaseOperateDataCallbackWrapper<User>(new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

                mBaseView.dismissDialog();

                mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.modify_user_operation)));

                refreshView();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mBaseView.dismissDialog();

                mBaseView.showToast(operationResult.getResultMessage(context));

            }
        }, this));

    }


    @Override
    public boolean isActive() {
        return mBaseView != null;
    }
}
