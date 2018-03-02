package com.winsun.fruitmix.list.data;

import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FileUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MediaInTweetRemoteDataSource extends BaseRemoteDataSourceImpl {

    private GroupRequestParam mGroupRequestParam;



    public MediaInTweetRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam) {
        super(iHttpUtil, httpRequestFactory);
        mGroupRequestParam = groupRequestParam;
    }

    boolean downloadMedia(Media media) throws MalformedURLException, IOException, SocketTimeoutException, NetworkException {

        HttpRequest httpRequest = media.getImageOriginalUrl(httpRequestFactory, mGroupRequestParam,"");

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return false;
        }

        ResponseBody responseBody = iHttpUtil.getResponseBody(httpRequest);

        return FileUtil.downloadMediaToOriginalPhotoFolder(responseBody, media);

    }

}
