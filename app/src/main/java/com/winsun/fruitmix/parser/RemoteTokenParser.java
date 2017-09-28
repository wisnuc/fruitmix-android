package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class RemoteTokenParser extends BaseRemoteDataParser implements RemoteDatasParser<String> {
    @Override
    public List<String> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

         return Collections.singletonList(new JSONObject(root).getString("token"));

    }
}
