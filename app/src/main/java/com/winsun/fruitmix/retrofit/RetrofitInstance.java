package com.winsun.fruitmix.retrofit;

import com.winsun.fruitmix.util.FNAS;
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

    private static final String BASE_URL = FNAS.Gateway + ":" + FNAS.PORT;
    private Retrofit retrofitInstance;

    public Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder().baseUrl(BASE_URL).client(createOKHTTPClient()).build();
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

                Request request = original.newBuilder().header(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT).build();

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
