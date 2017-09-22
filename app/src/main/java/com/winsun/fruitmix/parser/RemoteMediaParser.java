package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteMediaParser implements RemoteDatasParser<Media> {

    public static final String TAG = RemoteMediaParser.class.getSimpleName();

    @Override
    public List<Media> parse(String json) throws JSONException {

        List<Media> medias = new ArrayList<>();

        JSONArray jsonArray;

        Media media;

        jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject root = jsonArray.getJSONObject(i);

            media = new Media();

            media.setUuid(root.optString("hash"));

            String width = root.optString("w");
            String height = root.optString("h");

            media.setWidth(width);
            media.setHeight(height);

            String dateTime = root.optString("datetime");

            if (dateTime.equals("") || dateTime.length() < 10) {
                media.setTime(Util.DEFAULT_DATE);
            } else {

                try {

                    String year = dateTime.substring(0, 4);

                    if (!Util.isNumeric(year))
                        throw new NumberFormatException(year + " is not number");

                    String month = dateTime.substring(5, 7);

                    if (!Util.isNumeric(month))
                        throw new NumberFormatException(month + " is not number");

                    String day = dateTime.substring(8, 10);

                    if (!Util.isNumeric(day))
                        throw new NumberFormatException(day + " is not number");

                    media.setTime(year + "-" + month + "-" + day + dateTime.substring(10));

                } catch (NumberFormatException e) {

                    media.setTime(Util.DEFAULT_DATE);
                }
            }

            int orientationNumber = root.optInt("orient");

            if (orientationNumber == 0)
                orientationNumber = 1;

            media.setOrientationNumber(orientationNumber);

            media.setLocal(false);

            media.setType(root.optString("m"));

            media.setLatitude(root.optString("lat"));
            media.setLongitude(root.optString("long"));

            medias.add(media);

        }

        return medias;
    }


}
