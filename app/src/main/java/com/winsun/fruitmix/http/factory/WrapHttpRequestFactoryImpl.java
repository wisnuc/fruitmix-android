package com.winsun.fruitmix.http.factory;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/18.
 */

public class WrapHttpRequestFactoryImpl extends BaseHttpRequestFactoryImpl implements BaseHttpRequestFactory {

    public static final String TAG = WrapHttpRequestFactoryImpl.class.getSimpleName();

    private static final String CALL_STATION_THROUGH_CLOUD_PRE = "/c/v1/stations/";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_JSON = "/json";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE = "/pipe";

    @Override
    public HttpRequest createHttpGetRequest(String httpPath, boolean isPipe) {

        return createHttpGetRequestThroughPipe(getStationID(), httpPath, isPipe);
    }

    private HttpRequest createHttpGetRequestThroughPipe(String stationID, String httpPath, boolean isPipe) {

        Log.d(TAG, "createHttpGetRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        String queryStr = "";

        if (httpPath.contains("?")) {

            String[] splitResult = httpPath.split("\\?");

            httpPath = splitResult[0];

            queryStr = "&" + splitResult[1];
        }

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        String newHttpPath = CALL_STATION_THROUGH_CLOUD_PRE + stationID + (isPipe ? CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE : CALL_STATION_THROUGH_CLOUD_END_FOR_JSON) + "?resource=" + httpPathEncodeByBase64
                + "&method=GET" + queryStr;

        return createHttpGetRequestWithOutJWTHeader(newHttpPath);

    }

    private HttpRequest createHttpGetRequestWithOutJWTHeader(String httpPath) {

        HttpRequest httpRequest = new HttpRequest(createUrl(httpPath), Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getToken());

        return httpRequest;

    }

    @Override
    public HttpRequest createHttpPostRequest(String httpPath, String body) {

        return createHasBodyRequestThroughPipe(httpPath, Util.HTTP_POST_METHOD, body);
    }

    private HttpRequest createHasBodyRequestThroughPipe(String httpPath, String method, String body) {

        Log.d(TAG, "createHasBodyRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        HttpRequest httpRequest = new HttpRequest(createUrl(CALL_STATION_THROUGH_CLOUD_PRE + getStationID() + CALL_STATION_THROUGH_CLOUD_END_FOR_JSON), method);

        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getToken());

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        try {

            JSONObject jsonObject;

            if (body.isEmpty())
                jsonObject = new JSONObject();
            else
                jsonObject = new JSONObject(body);

            jsonObject.put("resource", httpPathEncodeByBase64);
            jsonObject.put("method", method);

            httpRequest.setBody(jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return httpRequest;
    }

    @Override
    public HttpRequest createGetRequestByPathWithoutToken(String httpPath) {
        return createHttpGetRequest(httpPath,false);
    }
}
