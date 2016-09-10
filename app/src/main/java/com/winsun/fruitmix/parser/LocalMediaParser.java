package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalMediaParser implements LocalDataParser<Media> {

    @Override
    public List<Media> parse(Cursor cursor) {

        return new ArrayList<>();
    }
}
