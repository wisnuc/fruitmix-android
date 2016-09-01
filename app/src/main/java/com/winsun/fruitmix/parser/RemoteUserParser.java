package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.User;

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
    public List<User> parse(String json)  {

        List<User> users = new ArrayList<>();
        String uuid;
        JSONArray jsonArray;
        JSONObject itemRaw;
        User user;

        try {
            jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                itemRaw = jsonArray.getJSONObject(i);

                user = new User();
                uuid = itemRaw.getString("uuid");
                user.setUuid(uuid);
                user.setUserName(itemRaw.getString("username"));
                user.setAvatar(itemRaw.getString("avatar"));
                if (itemRaw.has("email")) {
                    user.setEmail(itemRaw.getString("email"));
                }
                StringBuilder stringBuilder = new StringBuilder();
                String[] splitStrings = user.getUserName().split(" ");
                for (String splitString : splitStrings) {
                    stringBuilder.append(splitString.substring(0, 1).toUpperCase());
                }
                if (user.getDefaultAvatar() == null) {
                    user.setDefaultAvatar(stringBuilder.toString());
                }
                if (user.getDefaultAvatarBgColor() == null) {
                    user.setDefaultAvatarBgColor(String.valueOf(new Random().nextInt(3)));
                }

                users.add(user);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return users;
    }
}
