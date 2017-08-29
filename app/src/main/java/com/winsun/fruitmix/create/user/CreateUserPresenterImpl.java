package com.winsun.fruitmix.create.user;

import android.app.Activity;
import android.content.Context;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/22.
 */

public class CreateUserPresenterImpl implements CreateUserPresenter {

    private CreateUserView createUserView;

    private List<String> remoteUserNames;

    private UserDataRepository mUserDataRepository;

    private ThreadManager threadManagerImpl;

    public CreateUserPresenterImpl(CreateUserView createUserView, UserDataRepository userDataRepository) {

        this.createUserView = createUserView;
        this.mUserDataRepository = userDataRepository;

        remoteUserNames = new ArrayList<>();

        getUserInThread(mUserDataRepository);

    }

    private void getUserInThread(UserDataRepository userDataRepository) {

        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                for (User user : data) {
                    remoteUserNames.add(user.getUserName());
                }
            }
        });
    }

    @Override
    public void onDestroy() {

        createUserView = null;

    }

    @Override
    public void createUser(final Context context, CreateUserActivity.CreateUserViewModel createUserViewModel) {

        createUserView.hideSoftInput();

        if (!Util.getNetworkState(context)) {
            createUserView.showToast(context.getString(R.string.no_network));
            return;
        }

        final String userName = createUserViewModel.getUserName();

        if (remoteUserNames.contains(userName)) {

            createUserViewModel.userNameErrorEnable.set(true);
            createUserViewModel.userNameError.set(context.getString(R.string.username_not_unique));

            return;

        } else if (userName.isEmpty()) {

            createUserViewModel.userNameErrorEnable.set(true);
            createUserViewModel.userNameError.set(context.getString(R.string.empty_username));

            return;

        } else {
            createUserViewModel.userNameErrorEnable.set(false);
        }

        final String password = createUserViewModel.getUserPassword();

        String confirmPassword = createUserViewModel.getUserConfirmPassword();

        if (!password.equals(confirmPassword)) {

            createUserViewModel.userConfirmPasswordErrorEnable.set(true);
            createUserViewModel.userConfirmPasswordError.set(context.getString(R.string.not_same_password));

            return;

        } else {

            createUserViewModel.userConfirmPasswordErrorEnable.set(false);

        }

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
