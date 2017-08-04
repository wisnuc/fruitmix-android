package com.winsun.fruitmix.regeocode;

import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Address;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.parser.RemoteMediaAddressParser;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/5.
 */

public class AMapReverseGeocode implements ReverseGeocode {

    public static final String TAG = AMapReverseGeocode.class.getSimpleName();

    public static final String AMAP_REGEOCODE_URL = "http://restapi.amap.com/v3/geocode/regeo?key=8373302f9fcde931cbab8a3a3d5da158";

    public static final String PARAM_SEPARATOR = "&";

    public static final String LOCATION_SEPARATOR = "|";

    public static final String LONGITUDE_LATITUDE_SEPARATOR = ",";

    @Override
    public boolean reverseGeocodeLongitudeLatitudeInMedias(List<Media> medias) {

        StringBuilder urlBuilder = new StringBuilder(AMAP_REGEOCODE_URL);

        urlBuilder.append(PARAM_SEPARATOR);

        urlBuilder.append("location=");

        for (Media media : medias) {

            urlBuilder.append(media.getLongitude());
            urlBuilder.append(LONGITUDE_LATITUDE_SEPARATOR);
            urlBuilder.append(media.getLatitude());

            urlBuilder.append(LOCATION_SEPARATOR);

        }

        int lastLocationSeparator = urlBuilder.lastIndexOf(LOCATION_SEPARATOR);

        urlBuilder.replace(lastLocationSeparator, lastLocationSeparator + 1, PARAM_SEPARATOR);

        if (medias.size() > 1) {
            urlBuilder.append("batch=true");
        } else {
            urlBuilder.append("batch=false");
        }

        String url = urlBuilder.toString();

        Log.i(TAG, "reverseGeocodeLongitudeLatitudeInMedias: url: " + url);

        HttpRequest request = new HttpRequest(url, Util.HTTP_GET_METHOD);

        try {
            HttpResponse response = OkHttpUtil.getInstance().remoteCall(request);

            if (response.getResponseCode() == 200) {

                String responseData = response.getResponseData();

                JSONObject rootObject = new JSONObject(responseData);

                if (!rootObject.getString("infocode").equals("10000")) {
                    return false;
                }

                List<Address> addresses = new RemoteMediaAddressParser().parse(responseData);

                Media media;
                for (int i = 0; i < medias.size(); i++) {
                    media = medias.get(i);

                    media.setAddress(addresses.get(i));

                    Log.i(TAG, "reverseGeocodeLongitudeLatitudeInMedias: media longitude: " + media.getLongitude()
                            + " media latitude: " + media.getLatitude() + " media address: " + media.getAddress());

                }

                return true;

            }


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
