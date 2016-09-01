package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.model.User;

/**
 * Created by Administrator on 2016/8/31.
 */
public class UserDataParserFactory implements ParserFactory<User> {

    @Override
    public LocalDataParser<User> createLocalDataParser() {
        return new LocalUserParser();
    }

    @Override
    public RemoteDataParser<User> createRemoteDataParser() {
        return new RemoteUserParser();
    }
}
