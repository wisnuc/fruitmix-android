package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class RemoteTokenParser implements RemoteDatasParser<String> {
    @Override
    public List<String> parse(String json) throws JSONException {

         return Collections.singletonList(new JSONObject(json).getString("token"));

    }
}
