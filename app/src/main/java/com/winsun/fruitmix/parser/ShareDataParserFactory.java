package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.Share;

/**
 * Created by Administrator on 2016/9/1.
 */
public class ShareDataParserFactory implements ParserFactory<Share> {

    @Override
    public LocalDataParser<Share> createLocalDataParser() {
        return new LocalShareParser();
    }

    @Override
    public RemoteDataParser<Share> createRemoteDataParser() {
        return new RemoteShareParser();
    }
}
