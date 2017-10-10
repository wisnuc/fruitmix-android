package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;

/**
 * Created by Administrator on 2017/9/14.
 */

public interface StationsDataSource {

    void getStationsByWechatGUID(String guid, BaseLoadDataCallback<Station> callback);

    void getStationInfoByStationAPI(String ip, BaseLoadDataCallback<Station> callback);

}
