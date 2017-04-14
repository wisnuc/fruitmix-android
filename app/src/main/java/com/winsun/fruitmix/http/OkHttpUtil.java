package com.winsun.fruitmix.http;

import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.Util;

import java.io.File;
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
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Administrator on 2016/12/22.
 */

public class OkHttpUtil {

    public static final String TAG = OkHttpUtil.class.getSimpleName();

    private static OkHttpUtil Instance;

    private OkHttpClient okHttpClient;

    private List<Call> calls;

    private OkHttpUtil() {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).readTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).addInterceptor(createHttpInterceptor()).build();

        calls = new ArrayList<>();
    }

    public static OkHttpUtil getInstance() {

        if (Instance == null) {
            Instance = new OkHttpUtil();
        }

        return Instance;
    }

    private Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }

    public HttpResponse remoteCallMethod(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException {

        RequestBody requestBody;

        Request request = null;

        Request.Builder builder = generateRequestBuilder(httpRequest);

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

        return executeRequest(request);
    }

    private HttpResponse executeRequest(Request request) throws MalformedURLException, IOException, SocketTimeoutException {
        Log.d(TAG, "remoteCallMethod: before execute" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        ResponseBody body = null;

        try {

            Call call = okHttpClient.newCall(request);

            calls.add(call);

            Response response = call.execute();

            Log.d(TAG, "remoteCallMethod: after execute " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            String str = "";
            int responseCode = response.code();

            body = response.body();

            if (responseCode == 200) {
                str = body.string();
            }

            body.close();

            calls.remove(call);

            Log.d(TAG, "remoteCallMethod: after read response body" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            return new HttpResponse(responseCode, str);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {

            if (body != null)
                body.close();

        }

    }

    public boolean uploadFile(HttpRequest httpRequest, Media media) {

        Request.Builder builder = generateRequestBuilder(httpRequest);

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("sha256", media.getUuid())
                .addFormDataPart("file", media.getThumb(), RequestBody.create(MediaType.parse("image/jpeg"), new File(media.getThumb())))
                .build();

        Request request = builder.post(requestBody).build();

        HttpResponse httpResponse;
        try {
            httpResponse = executeRequest(request);

            return httpResponse.getResponseCode() == 200;

        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

    }

    private Request.Builder generateRequestBuilder(HttpRequest httpRequest) {
        Request.Builder builder = new Request.Builder().url(httpRequest.getUrl());

        String headerKey = httpRequest.getHeaderKey();
        if (headerKey != null) {
            builder.addHeader(headerKey, httpRequest.getHeaderValue());
        }
        return builder;
    }

    public void cancelAllNotFinishCall() {
        for (Call call : calls) {
            if (call != null)
                call.cancel();
        }
    }

}
