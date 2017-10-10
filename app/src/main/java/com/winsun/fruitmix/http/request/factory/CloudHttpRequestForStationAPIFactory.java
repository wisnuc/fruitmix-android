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

class CloudHttpRequestForStationAPIFactory extends CloudHttpRequestFactory {

    private String stationID;

    private static final String CALL_STATION_THROUGH_CLOUD_PRE = "/c/v1/stations/";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_JSON = "/json";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE = "/pipe";

    CloudHttpRequestForStationAPIFactory(HttpHeader httpHeader, String stationID) {
        super(httpHeader);

        this.stationID = stationID;
    }

    @Override
    public HttpRequest createHttpGetRequest(String httpPath, boolean isGetStream) {

        return createHttpGetRequestThroughPipe(stationID, httpPath, isGetStream);
    }

    private HttpRequest createHttpGetRequestThroughPipe(String stationID, String httpPath, boolean isGetStream) {

        Log.d(TAG, "createHttpGetRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        String queryStr = "";

        if (httpPath.contains("?")) {

            String[] splitResult = httpPath.split("\\?");

            httpPath = splitResult[0];

            queryStr = "&" + splitResult[1];
        }

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        String newHttpPath = CALL_STATION_THROUGH_CLOUD_PRE + stationID + (isGetStream ? CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE : CALL_STATION_THROUGH_CLOUD_END_FOR_JSON) + "?resource=" + httpPathEncodeByBase64
                + "&method=GET" + queryStr;

        HttpRequest httpRequest = new HttpRequest(createUrl(newHttpPath), Util.HTTP_GET_METHOD);

        setHeader(httpRequest);

        return httpRequest;

    }

    @Override
    public HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPostStream) {

        Log.d(TAG, "createHasBodyRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        HttpRequest httpRequest = new HttpRequest(createUrl(CALL_STATION_THROUGH_CLOUD_PRE + stationID + (isPostStream ? CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE : CALL_STATION_THROUGH_CLOUD_END_FOR_JSON)),
                Util.HTTP_POST_METHOD);

        setHeader(httpRequest);

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        try {

            JSONObject jsonObject;

            if (body.isEmpty())
                jsonObject = new JSONObject();
            else
                jsonObject = new JSONObject(body);

            jsonObject.put("resource", httpPathEncodeByBase64);
            jsonObject.put("method", Util.HTTP_POST_METHOD);

            httpRequest.setBody(jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return httpRequest;

    }
}
