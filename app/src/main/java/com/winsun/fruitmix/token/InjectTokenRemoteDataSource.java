package com.winsun.fruitmix.token;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;

/**
 * Created by Administrator on 2017/7/28.
 */


public class InjectTokenRemoteDataSource {

    public static TokenRemoteDataSource provideTokenRemoteDataSource(Context context){

        return new TokenRemoteDataSource(InjectHttp.provideIHttpUtil(context),InjectHttp.provideHttpRequestFactory());
    }

}
