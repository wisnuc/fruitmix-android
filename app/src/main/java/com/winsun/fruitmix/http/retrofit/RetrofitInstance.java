package com.winsun.fruitmix.http.retrofit;

import com.winsun.fruitmix.util.Util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by Administrator on 2016/11/3.
 */

public enum RetrofitInstance {

    INSTANCE;

    public static final String TAG = RetrofitInstance.class.getSimpleName();

    private String mToken;
    private Retrofit retrofitInstance;

    public Retrofit getRetrofitInstance(String baseUrl,String token) {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder().baseUrl(baseUrl).client(createOKHTTPClient()).build();
            mToken = token;
        }
        return retrofitInstance;
    }


    private OkHttpClient createOKHTTPClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(createHeaderInterceptor())
                .addInterceptor(createHttpInterceptor())
                .connectTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Util.HTTP_CONNECT_TIMEOUT,TimeUnit.SECONDS)
                .build();
    }

    private Interceptor createHeaderInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder().header(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + mToken).build();

                return chain.proceed(request);
            }
        };
    }

    private Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }
}
