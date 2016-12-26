package com.winsun.fruitmix.util;

import com.winsun.fruitmix.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Administrator on 2016/12/22.
 */

public enum OkHttpUtil {

    INSTANCE;

    private OkHttpClient okHttpClient;

    OkHttpUtil() {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).addInterceptor(createHttpInterceptor()).build();
    }

    private Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }

    public HttpResponse remoteCallMethod(String httpMethod, String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {

        RequestBody requestBody;

        Request request = null;

        switch (httpMethod) {
            case Util.HTTP_GET_METHOD:
                request = new Request.Builder().url(FNAS.Gateway + ":" + FNAS.PORT + req).addHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT).get().build();
                break;
            case Util.HTTP_POST_METHOD:
                requestBody = RequestBody.create(MediaType.parse("application/json"), data);
                request = new Request.Builder().url(FNAS.Gateway + ":" + FNAS.PORT + req).addHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT).post(requestBody).build();
                break;
            case Util.HTTP_DELETE_METHOD:
                requestBody = RequestBody.create(MediaType.parse("application/json"), data);
                request = new Request.Builder().url(FNAS.Gateway + ":" + FNAS.PORT + req).addHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT).delete(requestBody).build();
                break;
        }

        Response response = okHttpClient.newCall(request).execute();

        String str = "";
        int responseCode = response.code();

        if (responseCode == 200) {
            str = FNAS.ReadFull(response.body().byteStream());
        }

        return new HttpResponse(responseCode, str);
    }

}
