package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.model.RemoteFile;
import com.winsun.fruitmix.file.model.RemoteFolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFileFolderParser implements RemoteDataParser<AbstractRemoteFile>{

    public List<AbstractRemoteFile> parse(String json) {

        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0;i < jsonArray.length();i++){

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                AbstractRemoteFile abstractRemoteFile;

                String type = jsonObject.getString("type");
                if(type.equals("file")){
                    abstractRemoteFile = new RemoteFile();

                    String time = jsonObject.getString("mtime");
                    abstractRemoteFile.setTime(time);

                    String size = jsonObject.getString("size");
                    abstractRemoteFile.setSize(size);

                }else {
                    abstractRemoteFile = new RemoteFolder();
                }

                abstractRemoteFile.setUuid(jsonObject.getString("uuid"));

                abstractRemoteFile.setName(jsonObject.getString("name"));

                JSONArray ownerArray = jsonObject.getJSONArray("owner");
                for (int j = 0;j < ownerArray.length();j++){
                    abstractRemoteFile.addOwner(ownerArray.getString(j));
                }

                abstractRemoteFiles.add(abstractRemoteFile);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return abstractRemoteFiles;
    }
}
