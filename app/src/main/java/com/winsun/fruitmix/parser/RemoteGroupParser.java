package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/19.
 */

public class RemoteGroupParser extends BaseRemoteDataParser implements RemoteDatasParser<PrivateGroup> {

    @Override
    public List<PrivateGroup> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<PrivateGroup> groups = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(root);

        RemoteOneCommentParser remoteOneCommentParser = new RemoteOneCommentParser();

        RemoteOneGroupParser remoteOneGroupParser = new RemoteOneGroupParser();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            PrivateGroup group = remoteOneGroupParser.parse(jsonObject,remoteOneCommentParser);

            groups.add(group);
        }

        return groups;
    }


}
