package com.winsun.fruitmix.torrent.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteTorrentDownloadInfoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/13.
 */

public class TorrentRemoteDataSource extends BaseRemoteDataSourceImpl implements TorrentDataSource {

    public static final String GET_DOWNLOAD_INFO_PATH = "/download";

    public static final String POST_DOWNLOAD_TORRENT_PATH = "/download/torrent";

    public static final String POST_DOWNLOAD_MAGNET_PATH = "/download/magnet";

    public TorrentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getAllTorrentDownloadInfo(BaseLoadDataCallback<TorrentDownloadInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(GET_DOWNLOAD_INFO_PATH);

        wrapper.loadCall(httpRequest, callback, new RemoteTorrentDownloadInfoParser());

    }

    @Override
    public void postTorrentDownloadTask(String dirUUID, File torrent, BaseOperateDataCallback<TorrentRequestParam> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(POST_DOWNLOAD_TORRENT_PATH, "");

        Map<String, String> map = new HashMap<>();
        map.put("dirUUID", dirUUID);

        wrapper.operateCall(httpRequest, map, "torrent", torrent.getName(), torrent, callback, new RemoteDataParser<TorrentRequestParam>() {
            @Override
            public TorrentRequestParam parse(String json) throws JSONException {

                return new TorrentRequestParam("");
            }
        });

    }

    @Override
    public void postTorrentDownloadTask(String dirUUID, String magnetUrl, BaseOperateDataCallback<TorrentRequestParam> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("dirUUID", dirUUID);
            jsonObject.put("magnetURL", magnetUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(POST_DOWNLOAD_MAGNET_PATH, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<TorrentRequestParam>() {
            @Override
            public TorrentRequestParam parse(String json) throws JSONException {
                return new TorrentRequestParam("");
            }
        });

    }

    @Override
    public void pauseTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("op", "pause");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(GET_DOWNLOAD_INFO_PATH + "/" + torrentRequestParam.getId(),
                jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void resumeTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("op", "resume");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(GET_DOWNLOAD_INFO_PATH + "/" + torrentRequestParam.getId(),
                jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void deleteTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("op", "destroy");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(GET_DOWNLOAD_INFO_PATH + "/" + torrentRequestParam.getId(),
                jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }


}
