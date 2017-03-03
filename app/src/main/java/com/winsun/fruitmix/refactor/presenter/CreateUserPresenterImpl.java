package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.contract.CreateUserContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class CreateUserPresenterImpl implements CreateUserContract.CreateUserPresenter {

    private CreateUserContract.CreateUserView mView;

    private List<String> remoteUserNames;

    private DataRepository mRepository;

    public CreateUserPresenterImpl(DataRepository repository) {
        mRepository = repository;

        remoteUserNames = new ArrayList<>();
    }

    @Override
    public void createUser(String userName, String userPassword, String userConfirmPassword) {

        mView.hideSoftInput();

        if (!mView.isNetworkAlive()){
            mView.showNoNetwork();
            return;
        }

        if (userName.isEmpty()) {
            mView.showEmptyUserName();
            return;
        } else if (remoteUserNames.contains(userName)) {
            mView.showNotUniqueUserName();
            return;
        } else {
            mView.showCorrectUserNameFormat();
        }

        if (!userPassword.equals(userConfirmPassword)) {
            mView.showNotSamePassword();
            return;
        } else {
            mView.showCorrectPasswordFormat();
        }

        mView.showDialog();

        mRepository.createUser(userName, userPassword, new UserOperationCallback.OperateUserCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, User user) {

                mView.dismissDialog();
                mView.handleCreateUserSucceed();
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                mView.dismissDialog();
                mView.handleCreateUserFail(operationResult);
            }
        });

    }

    @Override
    public void attachView(CreateUserContract.CreateUserView view) {
        mView = view;
    }

    @Override
    public void detachView() {

        mView.dismissDialog();

        mView = null;
    }

    @Override
    public void loadUserNames() {

        mRepository.loadUsersInThread(new UserOperationCallback.LoadUsersCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, List<User> users) {

                for (User user : users) {
                    remoteUserNames.add(user.getUserName());
                }
            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });

    }

    @Override
    public void handleBackEvent() {

        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
