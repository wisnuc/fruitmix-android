package com.winsun.fruitmix.util;

import android.util.Log;

import com.winsun.fruitmix.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

enum OkHttpUtil {

    INSTANCE;

    public static final String TAG = OkHttpUtil.class.getSimpleName();

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

        Log.i(TAG, "remoteCallMethod: before execute" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        Response response = okHttpClient.newCall(request).execute();

        Log.i(TAG, "remoteCallMethod: after execute " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        String str = "";
        int responseCode = response.code();

        if (responseCode == 200) {
            str = response.body().string();
        }

        response.body().close();

        Log.i(TAG, "remoteCallMethod: after read response body" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        return new HttpResponse(responseCode, str);
    }

}
