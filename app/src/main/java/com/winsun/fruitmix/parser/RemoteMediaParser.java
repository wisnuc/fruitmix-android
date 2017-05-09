package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.Util;

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
    public List<Media> parse(String json) throws JSONException {

        List<Media> medias = new ArrayList<>();

        JSONArray jsonArray;
        JSONObject itemRaw;

        JSONObject itemObject;

        JSONArray item;

        Media media;

        jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            item = jsonArray.getJSONArray(i);

            media = new Media();

            media.setUuid(item.getString(0));

            itemObject = item.getJSONObject(1);

            boolean sharing = itemObject.optBoolean("permittedToShare");

            media.setSharing(sharing);

            itemRaw = itemObject.getJSONObject("metadata");

            String width = itemRaw.optString("width");
            String height = itemRaw.optString("height");

            media.setWidth(width);
            media.setHeight(height);

            String dateTime = itemRaw.optString("exifDateTime");

            if (dateTime.equals("")) {
                media.setTime(Util.DEFAULT_DATE);
            } else {
                media.setTime(dateTime.substring(0, 4) + "-" + dateTime.substring(5, 7) + "-" + dateTime.substring(8, 10));
            }

            int orientationNumber = itemRaw.optInt("exifOrientation");

            if (orientationNumber == 0)
                orientationNumber = 1;

            media.setOrientationNumber(orientationNumber);
            media.setLocal(false);

            media.setType(itemRaw.optString("format"));

            medias.add(media);

        }

        return medias;
    }
}
