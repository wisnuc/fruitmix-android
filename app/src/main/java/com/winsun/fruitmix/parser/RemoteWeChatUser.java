package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/9/20.
 */

public class RemoteWeChatUser extends BaseRemoteDataParser implements RemoteDatasParser<User> {

    @Override
    public List<User> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        User user = new User();

        user.setUserName(jsonObject.optString("nickName"));
        user.setAvatar(jsonObject.optString("avatarUrl"));

        return Collections.singletonList(user);
    }
}
