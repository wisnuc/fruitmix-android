package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/7/12.
 */

public class RemoteTicketParser implements RemoteDataParser<String> {

    public String parse(String json) throws JSONException {

        JSONObject root = new JSONObject(json);

        String url = root.getString("url");

        String[] result = url.split("/");

        return result[result.length - 1];
    }
}
