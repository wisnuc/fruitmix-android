package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteBuiltInDrive;
import com.winsun.fruitmix.file.data.model.RemotePrivateDrive;
import com.winsun.fruitmix.file.data.model.RemotePublicDrive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/16.
 */

public class RemoteRootDriveFolderParser extends BaseRemoteDataParser implements RemoteDatasParser<AbstractRemoteFile> {


    @Override
    public List<AbstractRemoteFile> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<AbstractRemoteFile> files = new ArrayList<>();

        JSONArray array = new JSONArray(root);

        for (int i = 0; i < array.length(); i++) {

            JSONObject object = array.getJSONObject(i);

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

                    Object obj = object.opt("writelist");

                    if (obj instanceof JSONArray) {

                        JSONArray writeList = (JSONArray) obj;

                        for (int j = 0; j < writeList.length(); j++)
                            file.addWriteList(writeList.optString(j));

                    } else if (obj instanceof String) {

                        file.addWriteList((String) obj);

                    } else
                        continue;

                    file.setName(object.optString("label"));

                    break;
            }

            file.setUuid(object.optString("uuid"));

            files.add(file);

        }

        return files;
    }


}
