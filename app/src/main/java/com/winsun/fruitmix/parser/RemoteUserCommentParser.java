package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/22.
 */

public class RemoteUserCommentParser extends BaseRemoteDataParser implements RemoteDatasParser<UserComment> {

    @Override
    public List<UserComment> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray jsonArray = new JSONArray(root);

        List<UserComment> userComments = new ArrayList<>();

        RemoteOneCommentParser remoteOneCommentParser = new RemoteOneCommentParser();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            UserComment userComment = remoteOneCommentParser.parse(jsonObject);

            userComments.add(userComment);

        }

        return userComments;
    }

}
