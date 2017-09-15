package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFileFolderParser extends BaseRemoteDataParser implements RemoteDatasParser<AbstractRemoteFile> {

    public List<AbstractRemoteFile> parse(String json) throws JSONException {

        String rootStr = checkHasWrapper(json);

        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();

        JSONObject root = new JSONObject(rootStr);

        JSONArray entries = root.getJSONArray("entries");

        int length = entries.length();
        int ownerLength;

        for (int i = 0; i < length; i++) {

            JSONObject jsonObject = entries.getJSONObject(i);

            AbstractRemoteFile abstractRemoteFile;

            String fileName = jsonObject.optString("name");

            String type = jsonObject.optString("type");
            if (type.equals("file")) {
                abstractRemoteFile = new RemoteFile();

                abstractRemoteFile.setFileTypeResID(FileUtil.getFileTypeResID(fileName));

                ((RemoteFile) abstractRemoteFile).setFileHash(jsonObject.optString("hash"));

            } else {
                abstractRemoteFile = new RemoteFolder();
            }

            String size = jsonObject.optString("size");
            abstractRemoteFile.setSize(size);

            String time = jsonObject.optString("mtime");
            abstractRemoteFile.setTime(time);

            abstractRemoteFile.setUuid(jsonObject.optString("uuid"));

            abstractRemoteFile.setName(fileName);

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
