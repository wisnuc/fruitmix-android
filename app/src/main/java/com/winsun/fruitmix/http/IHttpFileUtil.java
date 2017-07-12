package com.winsun.fruitmix.http;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface IHttpFileUtil {

    ResponseBody downloadFile(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException;

    boolean uploadFile(HttpRequest httpRequest, Media media);

}
