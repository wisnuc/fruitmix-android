package com.winsun.fruitmix.create.user;

import android.app.Activity;
import android.content.Context;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
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

public class CreateUserPresenterImpl implements CreateUserPresenter {

    private CreateUserView createUserView;

    private List<String> remoteUserNames;

    private UserDataRepository mUserDataRepository;

    public CreateUserPresenterImpl(CreateUserView createUserView,String currentLoginUserUUID, UserDataRepository userDataRepository) {

        this.createUserView = createUserView;
        this.mUserDataRepository = userDataRepository;

        remoteUserNames = new ArrayList<>();

        getUserInThread(currentLoginUserUUID,mUserDataRepository);

    }

    private void getUserInThread(String userUUID,UserDataRepository userDataRepository) {

        userDataRepository.getUsers(userUUID,new BaseLoadDataCallbackImpl<User>() {
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

        if (checkUserNameFirstWordIsIllegal(userName)) {

            createUserViewModel.userNameErrorEnable.set(true);
            createUserViewModel.userNameError.set(context.getString(R.string.username_has_illegal_character));

            return;
        }

        if (checkUserNameIsIllegal(userName)) {
            createUserViewModel.userNameErrorEnable.set(true);
            createUserViewModel.userNameError.set(context.getString(R.string.username_has_illegal_character));

            return;
        }

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


        if (checkPasswordFirstWordIsIllegal(password)) {

            createUserViewModel.userPasswordErrorEnable.set(true);
            createUserViewModel.userPasswordError.set(context.getString(R.string.password_has_illegal_character));

            return;
        }

        if (checkPasswordIsIllegal(password)) {

            createUserViewModel.userPasswordErrorEnable.set(true);
            createUserViewModel.userPasswordError.set(context.getString(R.string.password_has_illegal_character));

            return;

        }

        if (password.isEmpty()) {

            createUserViewModel.userPasswordErrorEnable.set(true);
            createUserViewModel.userPasswordError.set(context.getString(R.string.empty_password));

            return;

        } else if (!password.equals(confirmPassword)) {

            createUserViewModel.userPasswordErrorEnable.set(false);

            createUserViewModel.userConfirmPasswordErrorEnable.set(true);
            createUserViewModel.userConfirmPasswordError.set(context.getString(R.string.not_same_password));

            return;

        } else {

            createUserViewModel.userPasswordErrorEnable.set(false);
            createUserViewModel.userConfirmPasswordErrorEnable.set(false);

        }

        createUserView.showProgressDialog(String.format(context.getString(R.string.operating_title), context.getString(R.string.create_user)));

        insertUserInThread(context, userName, password);

    }

    private boolean checkUserNameFirstWordIsIllegal(String userName) {
        Pattern pattern = Pattern.compile("^[-.]");

        Matcher matcher = pattern.matcher(userName);

        return matcher.lookingAt();

    }


    private boolean checkUserNameIsIllegal(String userName) {

        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+|[!()\\-.?[\\\\]_`~@#\"']+|[\\u4E00-\\u9FFF\\u3400-\\u4dbf\\uf900-\\ufaff\\u3040-\\u309f\\uac00-\\ud7af]");

        Matcher matcher = pattern.matcher(userName);

        return matcher.replaceAll("").length() != 0;

    }

    private boolean checkPasswordFirstWordIsIllegal(String password) {
        Pattern pattern = Pattern.compile("^[-.]");

        Matcher matcher = pattern.matcher(password);

        return matcher.lookingAt();
    }

    private boolean checkPasswordIsIllegal(String password) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+|[!()\\-.?[\\\\]_`~@#\"']+");

        Matcher matcher = pattern.matcher(password);

        return matcher.replaceAll("").length() != 0;
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
