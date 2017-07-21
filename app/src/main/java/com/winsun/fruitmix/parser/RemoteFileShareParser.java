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
 * Created by Administrator on 2016/11/8.
 */

public class RemoteFileShareParser implements RemoteDatasParser<AbstractRemoteFile> {

    @Override
    public List<AbstractRemoteFile> parse(String json) throws JSONException {

        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            JSONArray readerArray = jsonObject.getJSONArray("readlist");
            JSONArray writerArray = jsonObject.getJSONArray("writelist");

            if (readerArray.length() == 0 && writerArray.length() == 0)
                continue;

            AbstractRemoteFile abstractRemoteFile;

            String type = jsonObject.optString("type");
            if (type.equals("file")) {
                abstractRemoteFile = new RemoteFile();
            } else {
                abstractRemoteFile = new RemoteFolder();
            }

            abstractRemoteFile.setUuid(jsonObject.optString("uuid"));

            abstractRemoteFile.setName(jsonObject.optString("name"));

            JSONArray ownerArray = jsonObject.getJSONArray("owner");
            for (int j = 0; j < ownerArray.length(); j++) {
                abstractRemoteFile.addOwner(ownerArray.getString(j));
            }

            for (int j = 0; j < readerArray.length(); j++) {
                abstractRemoteFile.addReadList(readerArray.getString(j));
            }

            for (int j = 0; j < writerArray.length(); j++) {
                abstractRemoteFile.addReadList(writerArray.getString(j));
            }

            String time = jsonObject.optString("mtime");
            abstractRemoteFile.setTime(time);

            abstractRemoteFiles.add(abstractRemoteFile);
        }

        return abstractRemoteFiles;
    }
}
