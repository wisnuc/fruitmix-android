package com.winsun.fruitmix.parser;

import android.util.Log;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/7/25.
 */

public class RemoteConfirmUserResultParser implements RemoteDataParser<String> {

    public static final String TAG = RemoteConfirmUserResultParser.class.getSimpleName();

    @Override
    public String parse(String json) throws JSONException {

        Log.d(TAG, "parse: " + json);

        return json;
    }
}
