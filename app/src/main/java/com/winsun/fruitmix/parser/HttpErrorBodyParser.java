package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/1.
 */

public class HttpErrorBodyParser implements RemoteDataParser<String> {

    public static final String UPLOAD_FILE_EXIST_CODE = "EEXIST";

    @Override
    public String parse(String json) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);

        return jsonObject.optString("code");
    }
}
