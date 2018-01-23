package com.winsun.fruitmix.network.change;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationInfoCallByStationAPI;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2017/9/28.
 */

public class NetworkChangeUseCase {

    public static final String TAG = NetworkChangeUseCase.class.getSimpleName();

    private SystemSettingDataSource systemSettingDataSource;

    private HttpRequestFactory httpRequestFactory;

    private NetworkStateManager networkStateManager;

    private StationsDataSource stationsDataSource;

    private TokenDataSource tokenDataSource;

    private LoginUseCase mLoginUseCase;

    public NetworkChangeUseCase(SystemSettingDataSource systemSettingDataSource, HttpRequestFactory httpRequestFactory,
                                NetworkStateManager networkStateManager, StationsDataSource stationsDataSource,
                                TokenDataSource tokenDataSource, LoginUseCase loginUseCase) {
        this.systemSettingDataSource = systemSettingDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.networkStateManager = networkStateManager;
        this.stationsDataSource = stationsDataSource;
        this.tokenDataSource = tokenDataSource;
        mLoginUseCase = loginUseCase;

    }

    public void handleNetworkChange() {

        Log.i(TAG, "handleNetworkChange: httpRequestFactory:" + httpRequestFactory);

        if (!mLoginUseCase.isAlreadyLogin()) {

            Log.i(TAG, "handleNetworkChange: not login,return");

            return;

        }

        NetworkState networkState = networkStateManager.getNetworkState();

        if (networkState.isWifiConnected()) {

            if (systemSettingDataSource.getLoginWithWechatCodeOrNot()) {

                Log.i(TAG, "handleNetworkChange: loginWithWechatCodeOrNot is true,check station");

                String guid = systemSettingDataSource.getCurrentLoginUserGUID();

                stationsDataSource.getStationsByWechatGUID(guid, new BaseLoadDataCallbackImpl<Station>() {
                    @Override
                    public void onSucceed(List<Station> data, OperationResult operationResult) {

                        String currentStationID = systemSettingDataSource.getCurrentLoginStationID();

                        for (Station station : data) {

                            if (station.getId().equals(currentStationID)) {

                                String currentIP = station.getFirstIp();

                                checkStation(currentIP, currentStationID);

                            }

                        }

                    }

                });

            } else {

                Log.i(TAG, "handleNetworkChange: loginWithWechatCodeOrNot is false,get currentIP");

                String currentIP = systemSettingDataSource.getCurrentEquipmentIp();

                if (currentIP.startsWith(Util.HTTP)) {

                    int start = currentIP.indexOf(Util.HTTP);

                    currentIP = currentIP.substring(start);

                }

                Log.d(TAG, "handleNetworkChange: currentIP: " + currentIP);

                stationsDataSource.getStationInfoByStationAPI(currentIP, new BaseLoadDataCallbackImpl<StationInfoCallByStationAPI>() {

                    @Override
                    public void onSucceed(List<StationInfoCallByStationAPI> data, OperationResult operationResult) {
                        super.onSucceed(data, operationResult);

                        Log.i(NetworkChangeUseCase.TAG, "get station info succeed: check station ip succeed");
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        super.onFail(operationResult);

                        Log.i(NetworkChangeUseCase.TAG, "get station info fail,check to wechat user");

                        handleCheckToWeChatUser();

                    }

                });

            }

        } else if (!networkState.isWifiConnected() && networkState.isMobileConnected()) {

            Log.i(TAG, "handleNetworkChange: no wifi connected,isMobileConnected is true,then check to wechat user");

            handleCheckToWeChatUser();

        }

    }

    private void checkStation(final String ip, final String stationID) {

        Log.i(TAG, "checkStation: ip: " + ip + " stationID: " + stationID);

        stationsDataSource.getStationInfoByStationAPI(ip, new BaseLoadDataCallbackImpl<StationInfoCallByStationAPI>() {
            @Override
            public void onSucceed(List<StationInfoCallByStationAPI> data, OperationResult operationResult) {

                StationInfoCallByStationAPI stationInfoCallByStationAPI = data.get(0);

                if (stationInfoCallByStationAPI.getId().equals(stationID)) {

                    tokenDataSource.getTokenThroughWAToken(new BaseLoadDataCallbackImpl<String>() {
                        @Override
                        public void onSucceed(List<String> data, OperationResult operationResult) {

                            checkToLocalUser(data.get(0), ip);

                        }

                    });

                }

            }

        });

    }

    private void handleCheckToWeChatUser() {
        String currentWAToken = systemSettingDataSource.getCurrentWAToken();

        if (!systemSettingDataSource.getLoginWithWechatCodeOrNot() && currentWAToken.length() != 0) {
            checkToWeChatUser(currentWAToken);
        }
    }

    private void checkToLocalUser(String token, String ip) {

        Log.i(TAG, "checkToLocalUser: ip:" + ip);

        httpRequestFactory.checkToLocalUser(token, ip);

        EventBus.getDefault().postSticky(new OperationEvent(Util.LOGIN_STATE_CHANGED, new OperationSuccess()));

    }

    private void checkToWeChatUser(String token) {

        Log.i(TAG, "checkToWeChatUser: token:" + token);

        httpRequestFactory.checkToWeChatUser(token);

        EventBus.getDefault().postSticky(new OperationEvent(Util.LOGIN_STATE_CHANGED, new OperationSuccess()));

    }

}
