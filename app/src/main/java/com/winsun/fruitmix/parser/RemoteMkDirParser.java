package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/27.
 */

public class RemoteMkDirParser extends BaseRemoteDataParser implements RemoteDataParser<AbstractRemoteFile> {

    @Override
    public AbstractRemoteFile parse(String json) throws JSONException {

        JSONObject data;

        if(json.startsWith("[")){

            JSONArray jsonArray = new JSONArray(json);

            data= jsonArray.getJSONObject(0).optJSONObject("data");

        }else {

            JSONObject jsonObject = new JSONObject(json);

            data = jsonObject.optJSONObject("data");

        }

        AbstractRemoteFile abstractRemoteFile = new RemoteFolder();

        abstractRemoteFile.setUuid(data.optString("uuid"));
        abstractRemoteFile.setName(data.optString("name"));
        abstractRemoteFile.setTime(data.optLong("mtime"));

        return abstractRemoteFile;
    }
}
