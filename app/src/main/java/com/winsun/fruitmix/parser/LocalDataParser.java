package com.winsun.fruitmix.parser;

import android.database.Cursor;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public interface LocalDataParser<T> {

    List<T> parse(Cursor cursor);

}
