package com.winsun.fruitmix.token.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.token.param.StationTokenParam;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface TokenDataSource {

    void getStationToken(StationTokenParam stationTokenParam, BaseLoadDataCallback<String> callback);

    void getCloudToken(String wechatCode, BaseLoadDataCallback<WeChatTokenUserWrapper> callback);

    void getStationTokenThroughCloudToken(BaseLoadDataCallback<String> callback);

    void getSCloudTokenThroughStationTokenWithoutThreadChange(String userGUID, BaseLoadDataCallback<String> callback);

}
