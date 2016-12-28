package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteUserParser implements RemoteDataParser<User> {

    @Override
    public List<User> parse(String json) throws JSONException {

        List<User> users = new ArrayList<>();
        String uuid;
        JSONArray jsonArray;
        JSONObject itemRaw;
        User user;

        jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            itemRaw = jsonArray.getJSONObject(i);

            user = new User();
            uuid = itemRaw.optString("uuid");
            user.setUuid(uuid);
            user.setUserName(itemRaw.optString("username"));

            String avatar = itemRaw.optString("avatar");
            if (avatar.equals("null")) {
                user.setAvatar("defaultAvatar.jpg");
            } else {
                user.setAvatar(avatar);
            }

            String email = itemRaw.optString("email");
            if (email.equals("null")) {
                user.setEmail("");
            } else {
                user.setEmail(email);
            }

            user.setAdmin(itemRaw.optBoolean("isAdmin"));

            if (user.getDefaultAvatar() == null) {
                user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));
            }
            if (user.getDefaultAvatarBgColor() == 0) {
                user.setDefaultAvatarBgColor(new Random().nextInt(3) + 1);
            }

            user.setHome(itemRaw.optString("home"));
            user.setLibrary(itemRaw.optString("library"));

            users.add(user);

        }

        return users;
    }
}
