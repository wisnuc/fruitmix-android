package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalMediaShareParser implements LocalDataParser<MediaShare>{

    @Override
    public List<MediaShare> parse(Cursor cursor) {
        return new ArrayList<>();
    }
}
