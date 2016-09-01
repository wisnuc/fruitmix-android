package com.winsun.fruitmix.parser;

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

    @Override
    public List<Media> parse(String json) {

        List<Media> medias = new ArrayList<>();

        String time;
        JSONArray jsonArray;
        JSONObject itemRaw;
        Media media;

        try {
            jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                itemRaw = jsonArray.getJSONObject(i);
                if (itemRaw.getString("kind").equals("image")) {

                    media = new Media();

                    media.setUuid(itemRaw.getString("hash"));
                    media.setTime("1916-01-01 00:00:00");
                    if (itemRaw.has("width")) {
                        media.setWidth(itemRaw.getString("width"));
                        media.setHeight(itemRaw.getString("height"));
                    } else if (itemRaw.getJSONObject("detail").has("width")) {
                        media.setWidth(itemRaw.getJSONObject("detail").getString("width"));
                        media.setHeight(itemRaw.getJSONObject("detail").getString("height"));
                    } else {
                        media.setWidth(itemRaw.getJSONObject("detail").getJSONObject("exif").getString("ExifImageWidth"));
                        media.setHeight(itemRaw.getJSONObject("detail").getJSONObject("exif").getString("ExifImageHeight"));
                        if (itemRaw.getJSONObject("detail").has("exif") && itemRaw.getJSONObject("detail").getJSONObject("exif").has("CreateDate")) {
                            time = itemRaw.getJSONObject("detail").getJSONObject("exif").getString("CreateDate");
                            media.setTime(time.substring(0, 4) + "-" + time.substring(5, 7) + "-" + time.substring(8));
                        } else media.setTime("1916-01-01 00:00:00");
                    }

                    medias.add(media);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return medias;
    }
}
