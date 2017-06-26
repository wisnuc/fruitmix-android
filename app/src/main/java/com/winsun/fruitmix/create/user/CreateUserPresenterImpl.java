package com.winsun.fruitmix.create.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/6/22.
 */

public class CreateUserPresenterImpl implements CreateUserPresenter {

    private CreateUserView createUserView;

    private List<String> remoteUserNames;

    public CreateUserPresenterImpl(CreateUserView createUserView) {

        this.createUserView = createUserView;

        int size = LocalCache.RemoteUserMapKeyIsUUID.size();
        remoteUserNames = new ArrayList<>(size);

        final Collection<User> users = new ArrayList<>(LocalCache.RemoteUserMapKeyIsUUID.values());
        for (User user : users) {
            remoteUserNames.add(user.getUserName());
        }
    }

    @Override
    public void onDestroy() {

        createUserView = null;

    }

    @Override
    public void createUser(Context context, CreateUserActivity.CreateUserViewModel createUserViewModel) {

        createUserView.hideSoftInput();

        if (!Util.getNetworkState(context)) {
            createUserView.showToast(context.getString(R.string.no_network));
            return;
        }

        String userName = createUserViewModel.getUserName();

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

        String password = createUserViewModel.getUserPassword();

        String confirmPassword = createUserViewModel.getUserConfirmPassword();

        if (!password.equals(confirmPassword)) {

            createUserViewModel.userConfirmPasswordErrorEnable.set(true);
            createUserViewModel.userConfirmPasswordError.set(context.getString(R.string.not_same_password));

            return;

        } else {

            createUserViewModel.userConfirmPasswordErrorEnable.set(false);

        }

        createUserView.showProgressDialog(String.format(context.getString(R.string.operating_title), context.getString(R.string.create_user)));

        FNAS.createRemoteUser(userName, password);

    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {
        createUserView.dismissDialog();
    }
}
