package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.Media;

/**
 * Created by Administrator on 2016/9/1.
 */
public class MediaDataParserFactory implements ParserFactory<Media> {

    @Override
    public LocalDataParser<Media> createLocalDataParser() {
        return new LocalMediaParser();
    }

    @Override
    public RemoteDataParser<Media> createRemoteDataParser() {
        return new RemoteMediaParser();
    }
}
