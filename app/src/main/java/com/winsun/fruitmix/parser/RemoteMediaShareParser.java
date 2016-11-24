package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.MediaShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteMediaShareParser implements RemoteDataParser<MediaShare> {

    @Override
    public List<MediaShare> parse(String json) throws JSONException {

        List<MediaShare> mediaShares = new ArrayList<>();

        JSONArray jsonArray;
        JSONObject itemRaw;

        jsonArray = new JSONArray(json);

        RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

        for (int i = 0; i < jsonArray.length(); i++) {

            itemRaw = jsonArray.getJSONObject(i);

            MediaShare mediaShare = parser.getRemoteMediaShare(itemRaw);

            mediaShares.add(mediaShare);
        }

        return mediaShares;
    }
}
