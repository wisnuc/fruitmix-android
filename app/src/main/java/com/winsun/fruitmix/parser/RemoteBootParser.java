package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.data.Boot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */

public class RemoteBootParser extends BaseRemoteDataParser implements RemoteDatasParser<Boot> {
    @Override
    public List<Boot> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject rootObject = new JSONObject(root);

        Boot boot = new Boot();
        boot.setCurrentFileSystemUUID(rootObject.optString("current"));

        return Collections.singletonList(boot);
    }
}
