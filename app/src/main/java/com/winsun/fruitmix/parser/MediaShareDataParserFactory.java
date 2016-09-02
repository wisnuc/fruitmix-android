package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.MediaShare;

/**
 * Created by Administrator on 2016/9/1.
 */
public class MediaShareDataParserFactory implements ParserFactory<MediaShare> {

    @Override
    public LocalDataParser<MediaShare> createLocalDataParser() {
        return new LocalMediaShareParser();
    }

    @Override
    public RemoteDataParser<MediaShare> createRemoteDataParser() {
        return new RemoteMediaShareParser();
    }
}
