package com.winsun.fruitmix.media.remote.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpFileUtil;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.parser.RemoteMediaStreamParser;
import com.winsun.fruitmix.util.FileUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaRemoteDataSource extends BaseRemoteDataSourceImpl {

    public static final String MEDIA_PARAMETER = "/media";

    private static StationMediaRemoteDataSource instance;

    private IHttpFileUtil iHttpFileUtil;

    public static StationMediaRemoteDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {

        if (instance == null)
            instance = new StationMediaRemoteDataSource(iHttpUtil, httpRequestFactory, iHttpFileUtil);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private StationMediaRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {
        super(iHttpUtil, httpRequestFactory);
        this.iHttpFileUtil = iHttpFileUtil;
    }

    /*
     * WISNUC API:GET MEDIA LIST
     */
    public void getMedia(BaseLoadDataCallback<Media> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(MEDIA_PARAMETER);

//        wrapper.loadCall(httpRequest, callback, new RemoteMediaParser());

        wrapper.loadCall(httpRequest,callback,new RemoteMediaStreamParser());

    }

    /*
     * WISNUC API:GET MEDIA
     */
    boolean downloadMedia(Media media) throws MalformedURLException, IOException, SocketTimeoutException, NetworkException {

        HttpRequest httpRequest = media.getImageOriginalUrl(httpRequestFactory);;

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return false;
        }

        ResponseBody responseBody = iHttpUtil.getResponseBody(httpRequest);

        return FileUtil.downloadMediaToOriginalPhotoFolder(responseBody, media);

    }

}
