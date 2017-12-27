package com.winsun.fruitmix.http;

import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface IHttpFileUtil {

    Request createUploadWithProgressRequest(HttpRequest httpRequest, RequestBody requestBody,FileUploadState fileUploadState);

}
