package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/3.
 */

public class RemoteCurrentUserParser extends BaseRemoteDataParser implements RemoteDatasParser<User> {

    @Override
    public List<User> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(root));

        return Collections.singletonList(user);
    }
}
