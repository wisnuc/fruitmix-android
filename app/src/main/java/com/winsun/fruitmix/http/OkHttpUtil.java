package com.winsun.fruitmix.http;

import android.util.Log;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static OkHttpClient okHttpClient;

    private static final String APPLICATION_JSON_STRING = "application/json";
    private static final String JPEG_STRING = "image/jpeg";


    private static final String SHA_256_STRING = "sha256";
    private static final String FILE_STRING = "file";

    static {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Util.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).addInterceptor(createHttpInterceptor()).build();
    }

    private static OkHttpUtil instance;

    private OkHttpUtil() {
    }

    public static OkHttpUtil getInstance() {

        if (instance == null)
            instance = new OkHttpUtil();

        return instance;
    }

    private static Interceptor createHttpInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
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
        }

        Response response = executeRequest(request);

        String str = "";

        int responseCode = response.code();

        if (checkResponseCode(handleResponse(response))) {

            str = response.body().string();

        }

        response.close();

        Log.d(TAG, "remoteCallMethod: after read response body" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        return new HttpResponse(responseCode, str);

    }

    private boolean checkResponseCode(int code) {
        return code == 200;
    }

    private Response executeRequest(Request request) throws MalformedURLException, IOException, SocketTimeoutException {
        Log.d(TAG, "remoteCallMethod: before execute" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        try {

            Call call = okHttpClient.newCall(request);

            Response response = call.execute();

            Log.d(TAG, "remoteCallMethod: after execute " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

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
    public ResponseBody downloadFile(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException,NetworkException{

        Request.Builder builder = generateRequestBuilder(httpRequest);

        Request request = builder.get().build();

        Response response = executeRequest(request);

        int code = handleResponse(response);

        if (checkResponseCode(code)) {
            return response.body();
        } else {
            throw new NetworkException("download api return http error code",code);
        }

    }

    @Override
    public HttpResponse uploadFile(HttpRequest httpRequest, LocalFile localFile) throws MalformedURLException, IOException, SocketTimeoutException{

        try {

            Request.Builder builder = generateRequestBuilder(httpRequest);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("size", Integer.valueOf(localFile.getSize()));
            jsonObject.put("sha256", localFile.getFileHash());

            String fileName = jsonObject.toString();

//            String fileName = "{" +
//                    "\"size\":" + localFile.getSize() + "," +
//                    "\"sha256\":\"" + localFile.getFileHash() + "\"" +
//                    "}";

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(localFile.getName(), fileName, RequestBody.create(MediaType.parse(JPEG_STRING), new File(localFile.getPath())))
                    .build();


            Request request = builder.post(requestBody).build();

            Response response = executeRequest(request);

            String str = "";

            int responseCode = response.code();

            if (checkResponseCode(handleResponse(response))) {

                str = response.body().string();

            }

            response.close();

            Log.d(TAG, "remoteCallMethod: after read response body" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            return new HttpResponse(responseCode, str);

        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }

    }

    @Override
    public HttpResponse createFolder(HttpRequest httpRequest, String folderName) {

        try {

            Request.Builder builder = generateRequestBuilder(httpRequest);

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(folderName, "{\"op\":\"mkdir\"}").build();

            Request request = builder.post(requestBody).build();

            Response response = executeRequest(request);

            String str = "";

            int responseCode = response.code();

            if (checkResponseCode(handleResponse(response))) {

                str = response.body().string();

            }

            response.close();

            return new HttpResponse(responseCode, str);

        } catch (IOException e) {
            e.printStackTrace();

            return null;
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

    private int handleResponse(Response response){

        int code = response.code();

        if (code == 200) {
            return code;
        } else {

            Log.d(TAG, "handleResponse: " + code);

/*            if (code == HttpURLConnection.HTTP_FORBIDDEN)
                EventBus.getDefault().post(new OperationEvent(Util.TOKEN_INVALID, new OperationNetworkException(code)));*/

            response.close();

            return code;
        }

    }

}
