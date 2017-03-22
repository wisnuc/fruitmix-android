package com.winsun.fruitmix.presenter;

import android.content.Intent;

import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.contract.AccountManageContract;
import com.winsun.fruitmix.model.LoggedInUser;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/3/21.
 */

public class AccountManagePresenterImpl implements AccountManageContract.AccountManagePresenter {

    private AccountManageContract.AccountManageView mView;

    private DataRepository mRepository;

    private List<String> mEquipmentNames;
    private List<List<LoggedInUser>> mUsers;

    public static final int START_EQUIPMENT_SEARCH = 0x1001;

    private boolean mNewUserLoginSucceed = false;
    private boolean mDeleteCurrentUser = false;
    private boolean mDeleteOtherUser = false;

    public AccountManagePresenterImpl(DataRepository repository) {
        this.mRepository = repository;

        mEquipmentNames = new ArrayList<>();
        mUsers = new ArrayList<>();

    }

    @Override
    public void attachView(AccountManageContract.AccountManageView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    private void handleBack() {
        if (mNewUserLoginSucceed) {
            mView.setResult(MainPagePresenterImpl.RESULT_FINISH_ACTIVITY);
        } else if (mDeleteCurrentUser) {
            mView.setResult(MainPagePresenterImpl.RESULT_LOGOUT);
        } else if (mDeleteOtherUser) {
            mView.setResult(MainPagePresenterImpl.RESULT_REFRESH_LOGGED_IN_USER);
        }
    }

    @Override
    public void handleBackEvent() {
        handleBack();
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == START_EQUIPMENT_SEARCH && resultCode == RESULT_OK) {

            mNewUserLoginSucceed = true;
            handleBack();
            mView.finishActivity();
        }
    }

    @Override
    public void initView() {

        fillData();

        mView.setData(mEquipmentNames, mUsers);

    }

    private void fillData() {

        LoggedInUser loggedInUser;

        List<LoggedInUser> loggedInUsers = mRepository.loadLoggedInUserInMemory();

        for (int i = 0; i < loggedInUsers.size(); i++) {

            loggedInUser = loggedInUsers.get(i);
            String equipmentName = loggedInUser.getEquipmentName();

            if (mEquipmentNames.contains(equipmentName)) {

                mUsers.get(mEquipmentNames.indexOf(equipmentName)).add(loggedInUser);

            } else {

                List<LoggedInUser> users = new ArrayList<>();
                users.add(loggedInUser);

                mEquipmentNames.add(equipmentName);
                mUsers.add(users);
            }

        }

    }

    @Override
    public void deleteUserOnClick(int groupPosition, int childPosition) {

        LoggedInUser loggedInUser = mUsers.get(groupPosition).get(childPosition);

        mRepository.deleteLoggedInUser(loggedInUser);

        if (loggedInUser.getUser().getUuid().equals(mRepository.loadCurrentLoginUserUUIDInMemory())) {
            mDeleteCurrentUser = true;
        } else {
            mDeleteOtherUser = true;
        }

        mUsers.get(groupPosition).remove(childPosition);
        mView.setData(mEquipmentNames, mUsers);


    }
}
