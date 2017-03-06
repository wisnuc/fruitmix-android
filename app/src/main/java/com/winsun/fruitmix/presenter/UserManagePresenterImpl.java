package com.winsun.fruitmix.presenter;

import android.content.Intent;

import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.contract.UserManageContract;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.Util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/3/3.
 */

public class UserManagePresenterImpl implements UserManageContract.UserManagePresenter {

    private UserManageContract.UserManageView mView;

    private DataRepository repository;

    public UserManagePresenterImpl(DataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addUserBtnClick() {

    }

    @Override
    public void initView() {

        List<User> users = new ArrayList<>(repository.loadUsersInMemory());

        if(users.size() == 0){
            mView.showNoContentUI();
            mView.dismissContentUI();
        }else {

            mView.dismissNoContentUI();
            mView.showContentUI();

            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    return Collator.getInstance(Locale.CHINESE).compare(lhs.getUserName(), (rhs.getUserName()));
                }
            });

            mView.showUsers(users);

        }
    }

    @Override
    public void attachView(UserManageContract.UserManageView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.KEY_CREATE_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            initView();
        }

    }
}
