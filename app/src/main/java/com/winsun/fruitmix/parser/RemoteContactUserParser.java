package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/7.
 */

public class RemoteContactUserParser extends BaseRemoteDataParser implements RemoteDatasParser<User> {

    private Random mRandom;

    public RemoteContactUserParser() {
        mRandom = new Random();
    }

    @Override
    public List<User> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray jsonArray = new JSONArray(root);

        int length = jsonArray.length();

        List<User> users = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            User user = new User();

            user.setAssociatedWeChatGUID(jsonObject.optString("id"));
            user.setUserName(jsonObject.optString("nickName"));
            user.setAvatar(jsonObject.optString("avatarUrl"));

            Util.setUserDefaultAvatar(user, mRandom);

            users.add(user);

        }

        return users;
    }
}
