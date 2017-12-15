package com.winsun.fruitmix.http;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/5/16.
 */

public interface IHttpUtil {

    HttpResponse remoteCall(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException;

    HttpResponse remoteCallWithFormData(HttpRequest httpRequest, Map<String,String> map) throws MalformedURLException, IOException, SocketTimeoutException;

    HttpResponse remoteCallWithFormData(HttpRequest httpRequest,String name,String fileName,File file)throws MalformedURLException, IOException, SocketTimeoutException;

    HttpResponse remoteCallWithFormData(HttpRequest httpRequest,Map<String,String> map,String name,String fileName,File file)throws MalformedURLException, IOException, SocketTimeoutException;


}
