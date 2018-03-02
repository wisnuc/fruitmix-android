package com.winsun.fruitmix.token.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/28.
 */


public class InjectTokenRemoteDataSource {

    public static TokenDataSource provideTokenDataSource(Context context){

        return new TokenDataRepository(ThreadManagerImpl.getInstance(),
                new TokenRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                        InjectHttp.provideHttpRequestFactory(context)));

    }

}
