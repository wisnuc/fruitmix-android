package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Administrator on 2017/1/18.
 */

public class RemoteUserJSONObjectParser {

    private Random random;

    private int preAvatarBgColor = 0;

    public RemoteUserJSONObjectParser() {

        random = new Random();

    }

    public User getUser(JSONObject itemRaw) throws JSONException {

        String uuid;
        User user;

        user = new User();
        uuid = itemRaw.optString("uuid");
        user.setUuid(uuid);
        user.setUserName(itemRaw.optString("username").trim());

        String avatar = itemRaw.optString("avatar");
        if (avatar.equals("null")) {
            user.setAvatar(User.DEFAULT_AVATAR);
        } else {
            user.setAvatar(avatar);
        }

        String email = itemRaw.optString("email").trim();
        if (email.equals("null")) {
            user.setEmail("");
        } else {
            user.setEmail(email);
        }

        user.setAdmin(itemRaw.optBoolean("isAdmin"));

        if (user.getDefaultAvatar() == null || user.getDefaultAvatar().isEmpty()) {
            user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));
        }
        if (user.getDefaultAvatarBgColor() == 0) {

            int avatarBgColor = random.nextInt(3) + 1;

            if (preAvatarBgColor != 0) {

                if (avatarBgColor == preAvatarBgColor) {
                    if (avatarBgColor == 3) {
                        avatarBgColor--;
                    } else if (avatarBgColor == 1) {
                        avatarBgColor++;
                    } else {
                        avatarBgColor++;
                    }
                }

                preAvatarBgColor = avatarBgColor;

            } else {
                preAvatarBgColor = avatarBgColor;
            }

            user.setDefaultAvatarBgColor(avatarBgColor);
        }

        user.setHome(itemRaw.optString("home"));
        user.setLibrary(itemRaw.optString("library"));

        JSONObject global = itemRaw.optJSONObject("global");
        if(global != null){
            user.setAssociatedWeChatGUID(global.optString("id"));
        }

        return user;
    }

}
