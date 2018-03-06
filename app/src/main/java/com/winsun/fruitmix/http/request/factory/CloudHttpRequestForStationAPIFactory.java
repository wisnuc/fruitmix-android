package com.winsun.fruitmix.http.request.factory;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/9.
 */

class CloudHttpRequestForStationAPIFactory extends BaseCloudHttpRequestForStationAPIFactory {

    CloudHttpRequestForStationAPIFactory(HttpHeader httpHeader, String stationID) {
        super(httpHeader, stationID);
    }

    @Override
    protected String getBaseUrlPathBeforePipeOrJson() {
        return CLOUD_API_LEVEL + CALL_STATION_THROUGH_CLOUD_PRE + stationID;
    }

}
