package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.contract.EquipmentSearchContract;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
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

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    private static final String SYSTEM_PORT = "3000";
    private static final String IPALIASING = "/system/ipaliasing";

    private List<List<User>> mUserExpandableLists;
    private List<Equipment> mUserLoadedEquipments;

    private List<Equipment> mFoundedEquipments;

    private Subscription mSubscription;

    private DataRepository mRepository;

    public EquipmentSearchPresenterImpl(DataRepository repository) {

        mRepository = repository;

        mUserExpandableLists = new ArrayList<>();
        mUserLoadedEquipments = new ArrayList<>();
        mFoundedEquipments = new ArrayList<>();
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
    public boolean onEquipmentListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        User user = mUserExpandableLists.get(groupPosition).get(childPosition);

        String gateway = "http://" + mUserLoadedEquipments.get(groupPosition).getHosts().get(0);
        String userGroupName = mUserLoadedEquipments.get(groupPosition).getServiceName();

        mView.login(gateway, userGroupName, user);

        return false;
    }

    @Override
    public void startDiscovery(RxDnssd rxDnssd) {
        mSubscription = rxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(rxDnssd.resolve())
                .compose(rxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        if (bonjourService.isLost()) return;

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
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
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
            public void onLoadSucceed(OperationResult result, List<EquipmentAlias> equipmentAliases) {

                List<String> hosts = equipment.getHosts();

                for (EquipmentAlias alias : equipmentAliases) {

                    String ip = alias.getIpv4();
                    if (!hosts.contains(ip)) {
                        hosts.add(ip);
                    }

                }

                String url = Util.HTTP + equipment.getHosts().get(0) + ":" + FNAS.PORT + Util.LOGIN_PARAMETER;

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
