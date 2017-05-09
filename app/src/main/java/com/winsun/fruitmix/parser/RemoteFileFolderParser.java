package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFileFolderParser implements RemoteDataParser<AbstractRemoteFile> {

    public List<AbstractRemoteFile> parse(String json) throws JSONException {

        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(json);

        int length = jsonArray.length();
        int ownerLength;

        for (int i = 0; i < length; i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            AbstractRemoteFile abstractRemoteFile;

            String type = jsonObject.optString("type");
            if (type.equals("file")) {
                abstractRemoteFile = new RemoteFile();
            } else {
                abstractRemoteFile = new RemoteFolder();
            }

            String size = jsonObject.optString("size");
            abstractRemoteFile.setSize(size);

            String time = jsonObject.optString("mtime");
            abstractRemoteFile.setTime(time);

            abstractRemoteFile.setUuid(jsonObject.optString("uuid"));

            abstractRemoteFile.setName(jsonObject.optString("name"));

/*
            JSONArray ownerArray = jsonObject.getJSONArray("owner");

            ownerLength = ownerArray.length();
            for (int j = 0; j < ownerLength; j++) {
                abstractRemoteFile.addOwner(ownerArray.getString(j));
            }
*/

            abstractRemoteFiles.add(abstractRemoteFile);
        }

        return abstractRemoteFiles;
    }
}
