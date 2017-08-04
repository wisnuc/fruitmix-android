package com.winsun.fruitmix.media.remote.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaRemoteDataSource extends BaseRemoteDataSourceImpl {

    public static final String MEDIA_PARAMETER = "/media";

    private static StationMediaRemoteDataSource instance;

    public static StationMediaRemoteDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {

        if(instance == null)
            instance = new StationMediaRemoteDataSource(iHttpUtil,httpRequestFactory);

        return instance;
    }

    private StationMediaRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void getMedia(BaseLoadDataCallback<Media> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(MEDIA_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteMediaParser());

    }

}
