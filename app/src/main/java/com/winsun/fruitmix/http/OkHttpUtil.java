package com.winsun.fruitmix.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.toolbox.ImageRequest;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
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

public class OkHttpUtil implements IHttpUtil, IHttpFileUtil {

    public static final String TAG = OkHttpUtil.class.getSimpleName();

    private OkHttpClient okHttpClient;

    private static final String APPLICATION_JSON_STRING = "application/json";
    private static final String JPEG_STRING = "image/*";

    private static OkHttpUtil instance;

    private OkHttpUtil() {

        okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(ImageRequest.DEFAULT_IMAGE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(ImageRequest.DEFAULT_IMAGE_TIMEOUT_MS, TimeUnit.MILLISECONDS).writeTimeout(Util.HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(createHttpInterceptor()).build();
    }

    public static OkHttpUtil getInstance() {

        if (instance == null)
            instance = new OkHttpUtil();

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private static Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        return loggingInterceptor;
    }

    @Override
    public HttpResponse remoteCall(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException {

        RequestBody requestBody;

        Request request = null;

        Request.Builder builder = generateRequestBuilder(httpRequest);

        switch (httpRequest.getHttpMethod()) {
            case Util.HTTP_GET_METHOD:
                request = builder.get().build();
                break;
            case Util.HTTP_POST_METHOD:
                requestBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_STRING), httpRequest.getBody());
                request = builder.post(requestBody).build();
                break;
            case Util.HTTP_DELETE_METHOD:
                requestBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_STRING), httpRequest.getBody());
                request = builder.delete(requestBody).build();
                break;
            case Util.HTTP_PATCH_METHOD:
                requestBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_STRING), httpRequest.getBody());
                request = builder.patch(requestBody).build();
                break;
            case Util.HTTP_PUT_METHOD:
                requestBody = RequestBody.create(MediaType.parse(APPLICATION_JSON_STRING), httpRequest.getBody());
                request = builder.put(requestBody).build();
                break;
        }

        Response response = executeRequest(request);

        HttpResponse httpResponse = getHttpResponse(response);

        Log.d(TAG, "remoteCallMethod: after read response body" + Util.formatDate(System.currentTimeMillis()));

        return httpResponse;

    }

    private boolean checkResponseCode(int code) {
        return code == 200;
    }

    private Response executeRequest(Request request) throws MalformedURLException, IOException, SocketTimeoutException {
        Log.d(TAG, "remoteCallMethod: before execute" + Util.formatDate(System.currentTimeMillis()));

        try {

            Call call = okHttpClient.newCall(request);

            Response response = call.execute();

            Log.d(TAG, "remoteCallMethod: after execute " + Util.formatDate(System.currentTimeMillis()));

            return response;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    public ResponseBody downloadFile(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException, NetworkException {

        Request.Builder builder = generateRequestBuilder(httpRequest);

        Request request = builder.get().build();

        Response response = executeRequest(request);

        int code = response.code();

        if (checkResponseCode(code)) {
            return response.body();
        } else {
            throw new NetworkException("download api return http error code", new HttpResponse(code, response.body().string()));
        }

    }

    @Override
    public RequestBody createFormDataRequestBody(List<TextFormData> textFormDatas, List<FileFormData> fileFormDatas) {

        RequestBody requestBody;

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (TextFormData textFormData : textFormDatas) {
            multipartBodyBuilder.addFormDataPart(textFormData.getName(), textFormData.getValue());
        }

        for (FileFormData fileFormData : fileFormDatas) {

            File file = fileFormData.getFile();

            multipartBodyBuilder.addFormDataPart(fileFormData.getName(), fileFormData.getFileName(), RequestBody.create(MediaType.parse(FileUtil.getMIMEType(file.getName())), file));

        }

        requestBody = multipartBodyBuilder.build();

        return requestBody;

    }

    @Override
    public Request createPostRequest(HttpRequest httpRequest, RequestBody requestBody) {

        Request.Builder builder = generateRequestBuilder(httpRequest);

        return builder.post(requestBody).build();
    }

    @Override
    public Request createUploadWithProgressRequest(HttpRequest httpRequest, RequestBody requestBody, FileUploadState fileUploadState) {
        Request.Builder builder;

        builder = generateRequestBuilder(httpRequest);

        return builder.post(new ProgressRequestBody(requestBody, fileUploadState)).build();
    }

    @Override
    public HttpResponse remoteCallRequest(Request request) throws MalformedURLException, IOException, SocketTimeoutException {

        Response response = executeRequest(request);

        HttpResponse httpResponse = getHttpResponse(response);

        Log.d(TAG, "remoteCallRequest: after read response body" + Util.formatDate(System.currentTimeMillis()));

        return httpResponse;
    }

    private Request.Builder generateRequestBuilder(HttpRequest httpRequest) {

        Request.Builder builder = new Request.Builder().url(httpRequest.getUrl());

        String headerKey = httpRequest.getHeaderKey();
        if (headerKey != null) {
            builder.addHeader(headerKey, httpRequest.getHeaderValue());
        }
        return builder;
    }

    private HttpResponse getHttpResponse(Response response) throws IOException {

        int code = response.code();

        Log.d(TAG, "response code: " + code);

        String bodyStr = response.body().string();

        Log.d(TAG, "getHttpResponse body: " + bodyStr);

        HttpResponse httpResponse = new HttpResponse(code, bodyStr);

        response.close();

        return httpResponse;

    }

}
