package com.winsun.fruitmix.network.change;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.http.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.util.Util;

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

    public NetworkChangeUseCase(SystemSettingDataSource systemSettingDataSource, HttpRequestFactory httpRequestFactory,
                                NetworkStateManager networkStateManager, StationsDataSource stationsDataSource, TokenDataSource tokenDataSource) {
        this.systemSettingDataSource = systemSettingDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.networkStateManager = networkStateManager;
        this.stationsDataSource = stationsDataSource;
        this.tokenDataSource = tokenDataSource;
    }

    public void handleNetworkChange() {

        NetworkState networkState = networkStateManager.getNetworkState();

        if (networkState.isWifiConnected()) {

            if (systemSettingDataSource.getLoginWithWechatCodeOrNot()) {

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

                Log.d(TAG, "handleNetworkChange: current ip: " + systemSettingDataSource.getCurrentEquipmentIp());

                String currentIP = systemSettingDataSource.getCurrentEquipmentIp();

                if(currentIP.startsWith(Util.HTTP)){

                    int start = currentIP.indexOf(Util.HTTP);

                    currentIP = currentIP.substring(start);

                }

                stationsDataSource.getStationInfoByStationAPI(currentIP, new BaseLoadDataCallbackImpl<Station>() {

                    @Override
                    public void onFail(OperationResult operationResult) {
                        super.onFail(operationResult);

                        handleCheckToWeChatUser();

                    }

                });

            }

        } else if (!networkState.isWifiConnected() && networkState.isMobileConnected()) {

            handleCheckToWeChatUser();

        }

    }

    private void checkStation(final String ip, final String stationID) {

        stationsDataSource.getStationInfoByStationAPI(ip, new BaseLoadDataCallbackImpl<Station>() {
            @Override
            public void onSucceed(List<Station> data, OperationResult operationResult) {

                Station station = data.get(0);

                if (station.getId().equals(stationID)) {

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

        systemSettingDataSource.setCurrentLoginToken(token);

        systemSettingDataSource.setCurrentEquipmentIp(Util.HTTP + ip);

        systemSettingDataSource.setLoginWithWechatCodeOrNot(false);

        httpRequestFactory.setCurrentData(token, Util.HTTP + ip);

        httpRequestFactory.setPort(HttpRequestFactory.STATION_PORT);
    }

    private void checkToWeChatUser(String token) {

        systemSettingDataSource.setCurrentLoginToken(token);

        systemSettingDataSource.setLoginWithWechatCodeOrNot(true);

        httpRequestFactory.setCurrentData(token, HttpRequestFactory.CLOUD_IP);

        httpRequestFactory.setStationID(systemSettingDataSource.getCurrentLoginStationID());

        httpRequestFactory.setPort(HttpRequestFactory.CLOUD_PORT);

    }


}
