package com.winsun.fruitmix.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/1.
 */

public class HttpErrorBodyParser extends BaseRemoteDataParser implements RemoteDataParser<String> {

    public static final String UPLOAD_FILE_EXIST_CODE = "EEXIST";

    public static final String SHA256_MISMATCH = "sha256 mismatch";

    @Override
    public String parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray jsonArray = new JSONArray(root);

        JSONObject jsonObject = jsonArray.getJSONObject(0);

        JSONObject error = jsonObject.optJSONObject("error");

        return error.optString("message");
    }
}

