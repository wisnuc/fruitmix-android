package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.Comment;

/**
 * Created by Administrator on 2016/9/2.
 */
public class MediaCommentDataParserFactory implements ParserFactory<Comment> {

    @Override
    public LocalDataParser<Comment> createLocalDataParser() {
        return new LocalMediaCommentParser();
    }

    @Override
    public RemoteDataParser<Comment> createRemoteDataParser() {
        return new RemoteMediaCommentParser();
    }
}
