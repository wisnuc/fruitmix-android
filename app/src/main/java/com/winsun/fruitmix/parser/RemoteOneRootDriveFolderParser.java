package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteBuiltInDrive;
import com.winsun.fruitmix.file.data.model.RemotePrivateDrive;
import com.winsun.fruitmix.file.data.model.RemotePublicDrive;

import org.json.JSONArray;
import org.json.JSONObject;

public class RemoteOneRootDriveFolderParser {

    public AbstractRemoteFile parse(JSONObject object){

        AbstractRemoteFile file;

        String tag = object.optString("tag");

        switch (tag) {
            case "home":

                file = new RemotePrivateDrive();

                break;
            case "built-in":

                file = new RemoteBuiltInDrive();

                String str = object.optString("writelist");

                file.addWriteList(str);

                break;
            default:

                file = new RemotePublicDrive();

                file.setName(object.optString("label"));

                Object obj = object.opt("writelist");

                if (obj instanceof JSONArray) {

                    JSONArray writeList = (JSONArray) obj;

                    for (int j = 0; j < writeList.length(); j++)
                        file.addWriteList(writeList.optString(j));

                } else if (obj instanceof String) {

                    file.addWriteList((String) obj);

                }else
                    return null;


        }

        file.setUuid(object.optString("uuid"));

        return file;

    }

}
