package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/7/12.
 */

public class RemoteTicketParser extends BaseRemoteDataParser implements RemoteDataParser<String> {

    public String parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        String url = jsonObject.getString("url");

        String[] result = url.split("/");

        return result[result.length - 1];
    }
}
