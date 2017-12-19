package com.winsun.fruitmix.torrent.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.FileFormData;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.TextFormData;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteTorrentDownloadInfoParser;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;

/**
 * Created by Administrator on 2017/12/13.
 */

public class TorrentRemoteDataSource extends BaseRemoteDataSourceImpl implements TorrentDataSource {

    public static final String GET_DOWNLOAD_INFO_PATH = "/download";

    public static final String POST_DOWNLOAD_TORRENT_PATH = "/download/torrent";

    public static final String POST_DOWNLOAD_MAGNET_PATH = "/download/magnet";

    public static final String DIR_UUID_KEY = "dirUUID";

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

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(POST_DOWNLOAD_TORRENT_PATH, "");

        TextFormData textFormData;
        FileFormData fileFormData;

        if (httpRequest.getBody().length() != 0) {

            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(httpRequest.getBody());
                jsonObject.put(DIR_UUID_KEY, dirUUID);
                jsonObject.put(Util.SIZE_STRING, torrent.length());
                jsonObject.put(Util.SHA_256_STRING, Util.calcSHA256OfFile(torrent.getAbsolutePath()));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            textFormData = new TextFormData(Util.MANIFEST_STRING, jsonObject != null ? jsonObject.toString() : "");

            fileFormData = new FileFormData("filename", torrent.getName(), torrent);

        } else {

            textFormData = new TextFormData(DIR_UUID_KEY, dirUUID);

            fileFormData = new FileFormData("torrent", torrent.getName(), torrent);

        }

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), Collections.singletonList(fileFormData), callback, new RemoteDataParser<TorrentRequestParam>() {
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
            jsonObject.put(DIR_UUID_KEY, dirUUID);
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
