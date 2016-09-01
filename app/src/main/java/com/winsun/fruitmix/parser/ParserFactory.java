package com.winsun.fruitmix.parser;

import android.database.Cursor;

/**
 * Created by Administrator on 2016/9/1.
 */
public interface ParserFactory<T> {

    LocalDataParser<T> createLocalDataParser();

    RemoteDataParser<T> createRemoteDataParser();
}
