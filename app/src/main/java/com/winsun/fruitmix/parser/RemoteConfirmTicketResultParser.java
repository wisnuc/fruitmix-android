package com.winsun.fruitmix.parser;

import android.util.Log;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/7/25.
 */

public class RemoteConfirmTicketResultParser implements RemoteDataParser<String> {

    public static final String TAG = RemoteConfirmTicketResultParser.class.getSimpleName();

    @Override
    public String parse(String json) throws JSONException {

        Log.d(TAG, "parse: " + json);

        return json;
    }
}
