package com.winsun.fruitmix.parser;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Administrator on 2017/12/26.
 */

public interface RemoteDataStreamParser<T> {

    List<T> parse(InputStream inputStream) throws JSONException;

}
