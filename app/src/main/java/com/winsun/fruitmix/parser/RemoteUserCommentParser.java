package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.UserComment;

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

        for (int i = 0;i < jsonArray.length();i++){

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String uuid = jsonObject.optString("uuid");
            long time = jsonObject.optLong("ctime");

            long index = jsonObject.optLong("index");


        }

        return new ArrayList<>();
    }

}
