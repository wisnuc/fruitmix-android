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

public class RemoteRootDriveFoldersParser extends BaseRemoteDataParser implements RemoteDatasParser<AbstractRemoteFile> {

    private RemoteOneRootDriveFolderParser mRemoteOneRootDriveFolderParser;

    public RemoteRootDriveFoldersParser(){
        mRemoteOneRootDriveFolderParser = new RemoteOneRootDriveFolderParser();
    }

    @Override
    public List<AbstractRemoteFile> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<AbstractRemoteFile> files = new ArrayList<>();

        JSONArray array = new JSONArray(root);

        for (int i = 0; i < array.length(); i++) {

            JSONObject object = array.getJSONObject(i);

            AbstractRemoteFile file = mRemoteOneRootDriveFolderParser.parse(object);

            files.add(file);

        }

        return files;
    }


}
