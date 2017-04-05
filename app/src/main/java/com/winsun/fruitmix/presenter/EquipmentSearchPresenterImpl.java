package com.winsun.fruitmix.presenter;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.EquipmentDiscoveryCallback;
import com.winsun.fruitmix.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.business.callback.OperationCallback;
import com.winsun.fruitmix.business.callback.UserOperationCallback;
import com.winsun.fruitmix.contract.EquipmentSearchContract;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.EquipmentAlias;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/2/8.
 */

public class EquipmentSearchPresenterImpl implements EquipmentSearchContract.EquipmentSearchPresenter {

    public static final String TAG = EquipmentSearchPresenterImpl.class.getSimpleName();

    private EquipmentSearchContract.EquipmentSearchView mView;

    private static final String SYSTEM_PORT = "3000";
    private static final String IPALIASING = "/system/ipaliasing";

    private static final String HTTP_CODE = "http://";

    private List<List<User>> mUserExpandableLists;
    private List<Equipment> mUserLoadedEquipments;

    private List<Equipment> mFoundedEquipments;

    private DataRepository mRepository;

    private boolean mShouldCallLogout;

    public EquipmentSearchPresenterImpl(DataRepository repository, boolean shouldCallLogout) {

        mRepository = repository;

        mUserExpandableLists = new ArrayList<>();
        mUserLoadedEquipments = new ArrayList<>();
        mFoundedEquipments = new ArrayList<>();

        mShouldCallLogout = shouldCallLogout;
    }

    @Override
    public boolean onEquipmentListViewGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        int count = mView.getGroupCount();
        for (int i = 0; i < count; i++) {

            if (i == groupPosition) {

                if (mView.isGroupExpanded(i)) {
                    mView.collapseGroup(i);
                } else {
                    mView.expandGroup(i);
                }

            } else {

                if (mView.isGroupExpanded(i)) {
                    mView.collapseGroup(i);
                }

            }
        }

        return true;
    }

    @Override
    public boolean onEquipmentListViewChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

        User user = mUserExpandableLists.get(groupPosition).get(childPosition);

        List<LoggedInUser> loggedInUsers = mRepository.loadLoggedInUserInMemory();

        for (final LoggedInUser loggedInUser : loggedInUsers) {

            if (loggedInUser.getUser().getUuid().equals(user.getUuid())) {

                Util.loginType = LoginType.SPLASH_SCREEN;

                if (mShouldCallLogout) {

                    mRepository.logout(new OperationCallback() {
                        @Override
                        public void onOperationSucceed(OperationResult result) {

                            login(groupPosition, loggedInUser);
                        }
                    });

                } else {
                    login(groupPosition, loggedInUser);
                }

                return true;
            }

        }

        String gateway = HTTP_CODE + mUserLoadedEquipments.get(groupPosition).getHosts().get(0);
        String userGroupName = mUserLoadedEquipments.get(groupPosition).getServiceName();

        mView.login(gateway, userGroupName, user);

        return false;
    }

    private void login(int groupPosition, LoggedInUser loggedInUser) {

        String gateway = "http://" + mUserLoadedEquipments.get(groupPosition).getHosts().get(0);

        mRepository.insertGatewayToMemory(gateway);
        mRepository.insertLoginUserUUIDToMemory(loggedInUser.getUser().getUuid());
        mRepository.insertTokenToMemory(loggedInUser.getToken());
        mRepository.insertDeviceIDToMemory(loggedInUser.getDeviceID());

        mRepository.insertDeviceIDToDB(loggedInUser.getDeviceID());
        mRepository.insertTokenToDB(loggedInUser.getToken());
        mRepository.insertLoginUserUUIDToDB(loggedInUser.getUser().getUuid());
        mRepository.insertGatewayToDB(gateway);

        mRepository.insertGatewayToServer(gateway);
        mRepository.insertTokenToServer(loggedInUser.getToken());
        mRepository.insertDeviceIdToServer(loggedInUser.getDeviceID());

        mRepository.checkAutoUpload();

        mRepository.loadUsersInThread(null);
        mRepository.loadMediasInThread(null);
        mRepository.loadMediaSharesInThread(null);

        mView.startMainPageActivity();

        mView.finishActivity();
    }

    @Override
    public void startDiscovery(RxDnssd rxDnssd) {

        mRepository.startDiscovery(rxDnssd, new EquipmentDiscoveryCallback() {
            @Override
            public void onEquipmentDiscovery(BonjourService bonjourService) {
                String serviceName = bonjourService.getServiceName();
                if (!serviceName.toLowerCase().contains("wisnuc")) return;

                if (bonjourService.getInet4Address() == null) return;
                String hostAddress = bonjourService.getInet4Address().getHostAddress();

                for (Equipment equipment : mFoundedEquipments) {
                    if (equipment == null || serviceName.equals(equipment.getServiceName()) || equipment.getHosts().contains(hostAddress)) {
                        return;
                    }
                }

                Equipment equipment = new Equipment();
                equipment.setServiceName(serviceName);
                Log.d(TAG, "host address:" + hostAddress);

                List<String> hosts = new ArrayList<>();
                hosts.add(hostAddress);

                equipment.setHosts(hosts);
                equipment.setPort(bonjourService.getPort());

                mFoundedEquipments.add(equipment);
                loadUsersAboutEquipment(equipment);
            }
        });

    }

    @Override
    public void stopDiscovery() {
        mRepository.stopDiscovery();
    }

    @Override
    public void attachView(EquipmentSearchContract.EquipmentSearchView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        mRepository.stopTimingRetrieveMediaShare();

        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.KEY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            mView.finishActivity();
        } else if (requestCode == Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE && resultCode == RESULT_OK) {

            String ip = data.getStringExtra(Util.KEY_MANUAL_INPUT_IP);

            List<String> hosts = new ArrayList<>();
            hosts.add(ip);

            Equipment equipment = new Equipment("Winsuc Appliction " + ip, hosts, 6666);
            loadUsersAboutEquipment(equipment);
        }
    }

    private void loadUsersAboutEquipment(final Equipment equipment) {

        for (Equipment equipment1 : mUserLoadedEquipments) {
            if (equipment1.getHosts().contains(equipment.getHosts().get(0)))
                return;
        }

        String url = Util.HTTP + equipment.getHosts().get(0) + ":" + SYSTEM_PORT + IPALIASING;

        Log.d(TAG, "login retrieve equipment alias:" + url);

        mRepository.loadEquipmentAlias(url, new LoadEquipmentAliasCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, Collection<EquipmentAlias> equipmentAliases) {

                List<String> hosts = equipment.getHosts();

                for (EquipmentAlias alias : equipmentAliases) {

                    String ip = alias.getIpv4();
                    if (!hosts.contains(ip)) {
                        hosts.add(ip);
                    }

                }

                String url = Util.HTTP + equipment.getHosts().get(0) + ":" + Util.PORT + Util.LOGIN_PARAMETER;

                Log.d(TAG, "login url:" + url);

                mRepository.loadUserByLoginApi(url, new UserOperationCallback.LoadUsersCallback() {
                    @Override
                    public void onLoadSucceed(OperationResult operationResult, List<User> users) {

                        if (users.isEmpty())
                            return;

                        mUserLoadedEquipments.add(equipment);
                        mUserExpandableLists.add(users);

                        if (mView != null)
                            mView.showEquipmentsAndUsers(mUserLoadedEquipments, mUserExpandableLists);
                    }

                    @Override
                    public void onLoadFail(OperationResult operationResult) {

                    }
                });
            }

            @Override
            public void onLoadFail(OperationResult result) {

            }
        });

    }
}
