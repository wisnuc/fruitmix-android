package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/4.
 */

public class RemoteInsertUserParser extends BaseRemoteDataParser implements RemoteDataParser<User> {
    @Override
    public User parse(String json) throws JSONException {

        String rootStr = checkHasWrapper(json);

        return new RemoteUserJSONObjectParser().getUser(new JSONObject(rootStr));
    }
}
