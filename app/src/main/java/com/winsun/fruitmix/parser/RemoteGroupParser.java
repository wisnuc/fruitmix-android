package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.PrivateGroup;

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

        for (int i = 0;i < jsonArray.length();i++){

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String uuid = jsonObject.optString("uuid");
            String name = jsonObject.optString("name");
            String owerUUID = jsonObject.optString("owner");
            long createTime = jsonObject.optLong("ctime");
            long mTime = jsonObject.optLong("mtime");

            PrivateGroup group = new PrivateGroup(uuid,name,owerUUID);

            groups.add(group);
        }

        return groups;
    }
}
