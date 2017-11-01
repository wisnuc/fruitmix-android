package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;

/**
 * Created by Administrator on 2017/9/14.
 */

public interface StationsDataSource {

    void getStationsByWechatGUID(String guid, BaseLoadDataCallback<Station> callback);

    void getStationInfoByStationAPI(String ip, BaseLoadDataCallback<StationInfoCallByStationAPI> callback);

    void checkStationIP(String ip, BaseOperateDataCallback<Boolean> callback);

}
