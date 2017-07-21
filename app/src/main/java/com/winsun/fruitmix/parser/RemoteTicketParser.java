package com.winsun.fruitmix.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 */

public class RemoteTicketParser implements RemoteDataParser<String>{

     public String parse(String json) throws JSONException {

        JSONObject root = new JSONObject(json);

        return root.getString("url");
    }
}
