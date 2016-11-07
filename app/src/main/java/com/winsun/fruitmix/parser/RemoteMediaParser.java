package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.Media;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteMediaParser implements RemoteDataParser<Media> {

    public static final String TAG = RemoteMediaParser.class.getSimpleName();

    @Override
    public List<Media> parse(String json) {

        List<Media> medias = new ArrayList<>();

        JSONArray jsonArray;
        JSONObject itemRaw;
        Media media;

        try {
            jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                itemRaw = jsonArray.getJSONObject(i);

                media = new Media();

                media.setUuid(itemRaw.optString("digest"));
                media.setWidth(itemRaw.optString("width"));
                media.setHeight(itemRaw.optString("height"));

                String dateTime = itemRaw.optString("exifDateTime");

                if (dateTime.equals("")) {
                    media.setTime("1916-01-01");
                } else {
                    media.setTime(dateTime.substring(0, 4) + "-" + dateTime.substring(5, 7) + "-" + dateTime.substring(8, 10));
                }

                String sharing = itemRaw.optString("sharing");
                media.setSharing(!sharing.equals("2") && !sharing.equals("6"));

                int orientationNumber = itemRaw.optInt("exifOrientation");

                if (orientationNumber == 0)
                    orientationNumber = 1;

                media.setOrientationNumber(orientationNumber);

                medias.add(media);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return medias;
    }
}
