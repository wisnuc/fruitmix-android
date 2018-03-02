package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/9.
 */

public class RemoteOneGroupParser {

    private Random mRandom;

    public RemoteOneGroupParser() {

        mRandom = new Random();

    }

    public PrivateGroup parse(JSONObject jsonObject,RemoteOneCommentParser remoteOneCommentParser){

        String uuid = jsonObject.optString("uuid");
        String name = jsonObject.optString("name");
        String ownerGUID = jsonObject.optString("owner");
        long createTime = jsonObject.optLong("ctime");
        long mTime = jsonObject.optLong("mtime");

        String stationId = jsonObject.optString("stationId");

        JSONArray usersJSONArray = jsonObject.optJSONArray("users");

        List<User> users = new ArrayList<>(usersJSONArray.length());

        for (int j = 0; j < usersJSONArray.length(); j++) {

            JSONObject userObject = usersJSONArray.optJSONObject(j);

            User user = new User();
            user.setAssociatedWeChatGUID(userObject.optString("id"));
            user.setUserName(userObject.optString("nickName"));
            user.setAvatar(userObject.optString("avatarUrl"));

            Util.setUserDefaultAvatar(user, mRandom);

            users.add(user);
        }

        JSONObject lastCommentJson = jsonObject.optJSONObject("tweet");

        PrivateGroup group = new PrivateGroup(uuid, name, ownerGUID, stationId,users);

        group.setCreateTime(createTime);
        group.setModifyTime(mTime);

        JSONObject stations = jsonObject.optJSONObject("station");

        group.setStationOnline(stations.optInt("isOnline") == 1);

        group.setStationName(stations.optString("name"));

        UserComment lastComment = remoteOneCommentParser.parse(lastCommentJson);

        if (lastComment != null) {

            Util.fillUserCommentUser(users, lastComment);

            lastComment.setGroupUUID(group.getUUID());
            lastComment.setStationID(group.getStationID());

            if(lastComment instanceof SystemMessageTextComment)
                ((SystemMessageTextComment) lastComment).fillAddOrDeleteUser(users);

            group.setLastComment(lastComment);

        }

        return group;
    }

}
