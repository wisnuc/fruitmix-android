package com.winsun.fruitmix.retrofit;

import com.winsun.fruitmix.util.FNAS;

import retrofit2.Retrofit;

/**
 * Created by Administrator on 2016/11/3.
 */

public enum  RetrofitInstance {

    INSTANCE;

    private static final String BASE_URL = FNAS.Gateway;
    private Retrofit retrofitInstance;

    public Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder().baseUrl(BASE_URL).build();
        }
        return retrofitInstance;
    }

}
