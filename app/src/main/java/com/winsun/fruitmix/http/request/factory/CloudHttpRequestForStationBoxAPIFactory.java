package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/5.
 */

public class CloudHttpRequestForStationBoxAPIFactory extends BaseCloudHttpRequestForStationAPIFactory {

    private String groupUUID;

    CloudHttpRequestForStationBoxAPIFactory(HttpHeader httpHeader, String stationID, String groupUUID) {
        super(httpHeader, stationID);
        this.groupUUID = groupUUID;
    }

    @Override
    protected String getBaseUrlPathBeforePipeOrJson() {

        return CLOUD_API_LEVEL + "/boxes/" + groupUUID + CALL_STATION_THROUGH_CLOUD_PRE + stationID;

    }

}
