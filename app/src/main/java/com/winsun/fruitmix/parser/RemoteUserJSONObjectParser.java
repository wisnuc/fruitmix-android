package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Administrator on 2017/1/18.
 */

public class RemoteUserJSONObjectParser {

    private Random random;

    public RemoteUserJSONObjectParser(){

        random = new Random();

    }

    public User getUser(JSONObject itemRaw) throws JSONException{

        String uuid;
        User user;

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
            user.setDefaultAvatarBgColor(random.nextInt(3) + 1);
        }

        user.setHome(itemRaw.optString("home"));
        user.setLibrary(itemRaw.optString("library"));

        return user;
    }

}
