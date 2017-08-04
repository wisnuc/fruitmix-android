package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/4.
 */

public class RemoteInsertUserParser implements RemoteDataParser<User> {
    @Override
    public User parse(String json) throws JSONException {
        return new RemoteUserJSONObjectParser().getUser(new JSONObject(json));
    }
}
