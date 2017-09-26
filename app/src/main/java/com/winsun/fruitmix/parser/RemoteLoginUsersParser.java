package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteLoginUsersParser extends BaseRemoteDataParser implements RemoteDatasParser<User> {

    @Override
    public List<User> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<User> users = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject itemRaw;

        RemoteUserJSONObjectParser remoteUserJSONObjectParser = new RemoteUserJSONObjectParser();
        jsonArray = new JSONArray(root);

        for (int i = 0; i < jsonArray.length(); i++) {
            itemRaw = jsonArray.getJSONObject(i);

            User user = remoteUserJSONObjectParser.getUser(itemRaw);

            users.add(user);

        }

        return users;
    }
}
