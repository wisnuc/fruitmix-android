package com.winsun.fruitmix.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/17.
 */

public class RemoteUserHomeParser implements RemoteDatasParser<String> {

    @Override
    public List<String> parse(String json) throws JSONException {

        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject drive = jsonArray.getJSONObject(i);

            if (drive.optString("tag").equals("home")) {
                return Collections.singletonList(drive.optString("uuid"));
            }

        }

        return Collections.emptyList();
    }
}
