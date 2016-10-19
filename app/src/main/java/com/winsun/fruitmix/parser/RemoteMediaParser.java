package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.util.LocalCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

                media.setUuid(itemRaw.getString("digest"));
                media.setWidth(itemRaw.getString("width"));
                media.setHeight(itemRaw.getString("height"));

                String dateTime = itemRaw.optString("exifDateTime");

                if (dateTime.equals("")) {
                    media.setTime("1916-01-01");
                } else {
                    media.setTime(dateTime.substring(0, 4) + "-" + dateTime.substring(5, 7) + "-" + dateTime.substring(8, 10));
                }

                String sharing = itemRaw.getString("sharing");
                media.setSharing(sharing.equals("1"));

                medias.add(media);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return medias;
    }
}
