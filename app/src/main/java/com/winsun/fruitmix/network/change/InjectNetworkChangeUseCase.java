package com.winsun.fruitmix.network.change;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource;

/**
 * Created by Administrator on 2017/9/28.
 */

public class InjectNetworkChangeUseCase {

    public static NetworkChangeUseCase provideInstance(Context context){

        return new NetworkChangeUseCase(InjectSystemSettingDataSource.provideSystemSettingDataSource(context),
                InjectHttp.provideHttpRequestFactory(context), InjectNetworkStateManager.provideNetworkStateManager(context),
                InjectStation.provideStationDataSource(context), InjectTokenRemoteDataSource.provideTokenDataSource(context));

    }

}
