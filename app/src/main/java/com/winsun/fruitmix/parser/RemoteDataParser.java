package com.winsun.fruitmix.parser;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/7/13.
 */

public interface RemoteDataParser<T> {

     T parse(String json) throws JSONException;
}
