package com.winsun.fruitmix.create.user;

import android.app.Activity;
import android.content.Context;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.BaseOperateUserPresenter;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/6/22.
 */

public class CreateUserPresenterImpl extends BaseOperateUserPresenter implements CreateUserPresenter {

    private CreateUserView createUserView;

    public CreateUserPresenterImpl(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource, CreateUserView createUserView) {
        super(userDataRepository, systemSettingDataSource);
        this.createUserView = createUserView;
    }

    @Override
    public void onDestroy() {

        createUserView = null;

    }

    @Override
    public void createUser(final Context context, OperateUserViewModel operateUserViewModel) {

        createUserView.hideSoftInput();

        if (!Util.isNetworkConnected(context)) {
            createUserView.showToast(context.getString(R.string.no_network));
            return;
        }

        final String userName = operateUserViewModel.getUserName();

        if (!checkOperateUserName(context, userName, operateUserViewModel))
            return;

        final String password = operateUserViewModel.getUserPassword();

        String confirmPassword = operateUserViewModel.getUserConfirmPassword();

        if (!checkOperateUserPassword(context, password, confirmPassword, operateUserViewModel))
            return;

        createUserView.showProgressDialog(String.format(context.getString(R.string.operating_title), context.getString(R.string.create_user)));

        insertUserInThread(context, userName, password);

    }

    private void insertUserInThread(final Context context, String userName, String password) {

        mUserDataRepository.insertUser(userName, password, new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

                createUserView.dismissDialog();

                createUserView.setResultCode(Activity.RESULT_OK);

                createUserView.finishView();

            }

            @Override
            public void onFail(final OperationResult result) {

                createUserView.dismissDialog();

                createUserView.showToast(result.getResultMessage(context));

            }
        });
    }

}
