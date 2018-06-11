package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteRootDriveFolderParser extends BaseRemoteDataParser implements RemoteDataParser<AbstractRemoteFile> {

    @Override
    public AbstractRemoteFile parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject object = new JSONObject(root);

        return new RemoteOneRootDriveFolderParser().parse(object);
    }
}
