package com.winsun.fruitmix.user;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/10/19.
 */

public class BaseOperateUserPresenter {

    private List<String> remoteUserNames;

    protected UserDataRepository mUserDataRepository;

    public BaseOperateUserPresenter() {
        remoteUserNames = new ArrayList<>();
    }

    public BaseOperateUserPresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource) {

        remoteUserNames = new ArrayList<>();

        this.mUserDataRepository = userDataRepository;

        getUserInThread(systemSettingDataSource.getCurrentLoginUserUUID(), userDataRepository);
    }

    private void getUserInThread(String userUUID, UserDataRepository userDataRepository) {

        userDataRepository.getUsers(userUUID, new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                for (User user : data) {
                    remoteUserNames.add(user.getUserName());
                }
            }
        });
    }

    protected boolean checkOperateUserName(Context context, String newUserName, OperateUserViewModel operateUserViewModel) {

        if (checkUserNameFirstWordIsIllegal(newUserName)) {

            operateUserViewModel.userNameErrorEnable.set(true);
            operateUserViewModel.userNameError.set(context.getString(R.string.username_has_illegal_character));

            return false;
        }

        if (checkUserNameIsIllegal(newUserName)) {
            operateUserViewModel.userNameErrorEnable.set(true);
            operateUserViewModel.userNameError.set(context.getString(R.string.username_has_illegal_character));

            return false;
        }

        if (remoteUserNames.contains(newUserName)) {

            operateUserViewModel.userNameErrorEnable.set(true);
            operateUserViewModel.userNameError.set(context.getString(R.string.username_not_unique));

            return false;

        } else if (newUserName.isEmpty()) {

            operateUserViewModel.userNameErrorEnable.set(true);
            operateUserViewModel.userNameError.set(context.getString(R.string.empty_username));

            return false;

        } else {
            operateUserViewModel.userNameErrorEnable.set(false);

            return true;
        }

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

    protected boolean checkOperateUserPassword(Context context, String newPassword, String confirmPassword, OperateUserViewModel operateUserViewModel) {

        if (checkPasswordFirstWordIsIllegal(newPassword)) {

            operateUserViewModel.userPasswordErrorEnable.set(true);
            operateUserViewModel.userPasswordError.set(context.getString(R.string.password_has_illegal_character));

            return false;
        }

        if (checkPasswordIsIllegal(newPassword)) {

            operateUserViewModel.userPasswordErrorEnable.set(true);
            operateUserViewModel.userPasswordError.set(context.getString(R.string.password_has_illegal_character));

            return false;

        }

        if (newPassword.isEmpty()) {

            operateUserViewModel.userPasswordErrorEnable.set(true);
            operateUserViewModel.userPasswordError.set(context.getString(R.string.empty_password));

            return false;

        } else if (!newPassword.equals(confirmPassword)) {

            operateUserViewModel.userPasswordErrorEnable.set(false);

            operateUserViewModel.userConfirmPasswordErrorEnable.set(true);
            operateUserViewModel.userConfirmPasswordError.set(context.getString(R.string.not_same_password));

            return false;

        } else {

            operateUserViewModel.userPasswordErrorEnable.set(false);
            operateUserViewModel.userConfirmPasswordErrorEnable.set(false);

            return true;
        }

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


}
