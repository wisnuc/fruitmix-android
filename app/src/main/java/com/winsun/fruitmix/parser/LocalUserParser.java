package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalUserParser implements LocalDataParser<User> {

    @Override
    public List<User> parse(Cursor cursor) {
        return new ArrayList<>();
    }
}
