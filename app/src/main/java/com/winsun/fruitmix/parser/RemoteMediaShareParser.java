package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteMediaShareParser implements RemoteDataParser<MediaShare> {

    @Override
    public List<MediaShare> parse(String json) {

        List<MediaShare> mediaShares = new ArrayList<>();

        JSONArray jsonArray;
        JSONObject itemRaw;

        try {
            jsonArray = new JSONArray(json);

            RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

            for (int i = 0; i < jsonArray.length(); i++) {

                itemRaw = jsonArray.getJSONObject(i);

                MediaShare mediaShare = parser.getRemoteMediaShare(itemRaw);

                mediaShares.add(mediaShare);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaShares;
    }
}
