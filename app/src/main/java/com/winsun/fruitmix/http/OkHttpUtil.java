package com.winsun.fruitmix.http;

import android.util.Log;

import com.winsun.fruitmix.util.Util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Administrator on 2016/12/22.
 */

public enum OkHttpUtil {

    INSTANCE;

    public static final String TAG = OkHttpUtil.class.getSimpleName();

    private OkHttpClient okHttpClient;

    private List<Call> calls;

    OkHttpUtil() {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS).readTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS).addInterceptor(createHttpInterceptor()).build();

        calls = new ArrayList<>();
    }

    private Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }

    public HttpResponse remoteCallMethod(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException {

        RequestBody requestBody;

        Request request = null;

        Request.Builder builder = new Request.Builder().url(httpRequest.getUrl());

        String headerKey = httpRequest.getHeaderKey();
        if (headerKey != null) {
            builder.addHeader(headerKey, httpRequest.getHeaderValue());
        }

        switch (httpRequest.getHttpMethod()) {
            case Util.HTTP_GET_METHOD:
                request = builder.get().build();
                break;
            case Util.HTTP_POST_METHOD:
                requestBody = RequestBody.create(MediaType.parse("application/json"), httpRequest.getBody());
                request = builder.post(requestBody).build();
                break;
            case Util.HTTP_DELETE_METHOD:
                requestBody = RequestBody.create(MediaType.parse("application/json"), httpRequest.getBody());
                request = builder.delete(requestBody).build();
                break;
            case Util.HTTP_PATCH_METHOD:
                requestBody = RequestBody.create(MediaType.parse("application/json"), httpRequest.getBody());
                request = builder.patch(requestBody).build();
                break;
        }

        Log.d(TAG, "remoteCallMethod: before execute" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        Call call = okHttpClient.newCall(request);

        calls.add(call);

        Response response = call.execute();

        Log.d(TAG, "remoteCallMethod: after execute " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        String str = "";
        int responseCode = response.code();

        if (responseCode == 200) {
            str = response.body().string();
        }

        response.body().close();

        calls.remove(call);

        Log.d(TAG, "remoteCallMethod: after read response body" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        return new HttpResponse(responseCode, str);
    }

    public void cancelAllNotFinishCall() {
        for (Call call : calls) {
            if (call != null)
                call.cancel();
        }
    }

}
