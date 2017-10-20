package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/19.
 */

public class RemoteFillBindWeChatUserTicketParser extends BaseRemoteDataParser implements RemoteDataParser<String> {
    @Override
    public String parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject rootStr = new JSONObject(root);

        return rootStr.optString("userId");
    }
}
