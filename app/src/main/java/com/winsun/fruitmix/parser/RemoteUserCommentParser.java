package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.UserComment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/3/19.
 */

public class RemoteUserCommentParser extends BaseRemoteDataParser implements RemoteDataParser<UserComment> {
    @Override
    public UserComment parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        RemoteOneCommentParser parser = new RemoteOneCommentParser();

        return parser.parse(new JSONObject(root));

    }
}
