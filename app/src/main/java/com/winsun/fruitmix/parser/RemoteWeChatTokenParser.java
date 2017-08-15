package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/10.
 */

public class RemoteWeChatTokenParser implements RemoteDatasParser<String> {

    public List<String> parse(String json) throws JSONException {

        JSONObject root = new JSONObject(json);

        JSONObject data = root.getJSONObject("data");

        JSONObject wechat = data.getJSONObject("wechat");

        String nickName = wechat.optString("nickName");
        String avatarUrl = wechat.optString("avatarUrl");

        User user = new User();
        user.setUserName(nickName);
        user.setAvatar(avatarUrl);

        return Collections.singletonList("");
    }

}
