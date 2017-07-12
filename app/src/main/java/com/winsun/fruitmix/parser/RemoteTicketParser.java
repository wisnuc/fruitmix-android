package com.winsun.fruitmix.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 */

public class RemoteTicketParser {

    public String parse(String json) throws JSONException {

        JSONArray root = new JSONArray(json);

        JSONObject ticket = root.getJSONObject(0);

        return ticket.optString("uuid");
    }
}
