package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.Share;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalShareParser implements LocalDataParser<Share>{

    @Override
    public List<Share> parse(Cursor cursor) {
        return new ArrayList<>();
    }
}
