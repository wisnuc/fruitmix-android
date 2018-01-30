package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/19.
 */

public class RemoteGroupParser extends BaseRemoteDataParser implements RemoteDatasParser<PrivateGroup> {

    @Override
    public List<PrivateGroup> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<PrivateGroup> groups = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(root);

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String uuid = jsonObject.optString("uuid");
            String name = jsonObject.optString("name");
            String ownerUUID = jsonObject.optString("owner");
            long createTime = jsonObject.optLong("ctime");
            long mTime = jsonObject.optLong("mtime");

            JSONArray usersJSONArray = jsonObject.optJSONArray("users");

            List<User> users = new ArrayList<>(usersJSONArray.length());

            for (int j = 0; j < usersJSONArray.length(); j++) {

                User user = new User();
                user.setAssociatedWeChatGUID(usersJSONArray.optString(j));

                users.add(user);
            }

            PrivateGroup group = new PrivateGroup(uuid, name, ownerUUID, users);

            group.setCreateTime(createTime);
            group.setModifyTime(mTime);

            groups.add(group);
        }

        return groups;
    }
}
