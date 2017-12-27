package com.winsun.fruitmix.http;

import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/5/16.
 */

public interface IHttpUtil {

    HttpResponse remoteCall(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException;

    RequestBody createFormDataRequestBody(List<TextFormData> textFormDatas, List<FileFormData> fileFormDatas);

    Request createPostRequest(HttpRequest httpRequest,RequestBody requestBody);

    HttpResponse remoteCallRequest(Request request) throws MalformedURLException, IOException, SocketTimeoutException;

    ResponseBody getResponseBody(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException,NetworkException;

}
